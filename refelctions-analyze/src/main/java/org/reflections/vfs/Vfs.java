package org.reflections.vfs;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.Utils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;

/**
 * a simple virtual file system bridge
 * <p>use the {@link Vfs#fromURL(URL)} to get a {@link Dir},
 * then use {@link Dir#getFiles()} to iterate over the {@link File}
 * <p>for example:
 * <pre>
 *      Vfs.Dir dir = Vfs.fromURL(url);
 *      Iterable<Vfs.File> files = dir.getFiles();
 *      for (Vfs.File file : files) {
 *          InputStream is = file.openInputStream();
 *      }
 * </pre>
 * <p>{@link Vfs#fromURL(URL)} uses static {@link DefaultUrlTypes} to resolve URLs.
 * It contains VfsTypes for handling for common resources such as local jar file, local directory, jar url, jar input stream and more.
 * <p>It can be plugged in with other {@link UrlType} using {@link Vfs#addDefaultURLTypes(UrlType)} or {@link Vfs#setDefaultURLTypes(List)}.
 * <p>for example:
 * <pre>
 *      Vfs.addDefaultURLTypes(new Vfs.UrlType() {
 *          public boolean matches(URL url)         {
 *              return url.getProtocol().equals("http");
 *          }
 *          public Vfs.Dir createDir(final URL url) {
 *              return new HttpDir(url); //implement this type... (check out a naive implementation on VfsTest)
 *          }
 *      });
 *
 *      Vfs.Dir dir = Vfs.fromURL(new URL("http://mirrors.ibiblio.org/pub/mirrors/maven2/org/slf4j/slf4j-api/1.5.6/slf4j-api-1.5.6.jar"));
 * </pre>
 * <p>use {@link Vfs#findFiles(Collection, com.google.common.base.Predicate)} to get an
 * iteration of files matching given name predicate over given list of urls
 */
public abstract class Vfs {
    private static List<UrlType> defaultUrlTypes = Lists.<UrlType>newArrayList(DefaultUrlTypes.values());

    /** an abstract vfs dir */
    public interface Dir {
        String getPath();
        Iterable<File> getFiles();
        void close();
    }

    /** an abstract vfs file */
    public interface File {
        String getName();
        String getRelativePath();
        InputStream openInputStream() throws IOException;
    }

    /** a matcher and factory for a url */
    public interface UrlType {
        boolean matches(URL url) throws Exception;
        Dir createDir(URL url) throws Exception;
    }

    /** the default url types that will be used when issuing {@link Vfs#fromURL(URL)} */
    public static List<UrlType> getDefaultUrlTypes() {
        return defaultUrlTypes;
    }

    /** sets the static default url types. can be used to statically plug in urlTypes */
    public static void setDefaultURLTypes(final List<UrlType> urlTypes) {
        defaultUrlTypes = urlTypes;
    }

    /** add a static default url types to the beginning of the default url types list. can be used to statically plug in urlTypes */
    public static void addDefaultURLTypes(UrlType urlType) {
        defaultUrlTypes.add(0, urlType);
    }

    /** tries to create a Dir from the given url, using the defaultUrlTypes */
    public static Dir fromURL(final URL url) {
        return fromURL(url, defaultUrlTypes);
    }

    /** tries to create a Dir from the given url, using the given urlTypes*/
    public static Dir fromURL(final URL url, final List<UrlType> urlTypes) {
        for (UrlType type : urlTypes) {
            try {
                if (type.matches(url)) {
                    Dir dir = type.createDir(url);
                    if (dir != null) return dir;
                }
            } catch (Throwable e) {
                if (Reflections.log != null) {
                    Reflections.log.warn("could not create Dir using " + type + " from url " + url.toExternalForm() + ". skipping.", e);
                }
            }
        }

        throw new ReflectionsException("could not create Vfs.Dir from url, no matching UrlType was found [" + url.toExternalForm() + "]\n" +
                "either use fromURL(final URL url, final List<UrlType> urlTypes) or " +
                "use the static setDefaultURLTypes(final List<UrlType> urlTypes) or addDefaultURLTypes(UrlType urlType) " +
                "with your specialized UrlType.");
    }

    /** tries to create a Dir from the given url, using the given urlTypes*/
    public static Dir fromURL(final URL url, final UrlType... urlTypes) {
        return fromURL(url, Lists.<UrlType>newArrayList(urlTypes));
    }

    /** return an iterable of all {@link File} in given urls, starting with given packagePrefix and matching nameFilter */
    public static Iterable<File> findFiles(final Collection<URL> inUrls, final String packagePrefix, final Predicate<String> nameFilter) {
        Predicate<File> fileNamePredicate = new Predicate<File>() {
            public boolean apply(File file) {
                String path = file.getRelativePath();
                if (path.startsWith(packagePrefix)) {
                    String filename = path.substring(path.indexOf(packagePrefix) + packagePrefix.length());
                    return !Utils.isEmpty(filename) && nameFilter.apply(filename.substring(1));
                } else {
                    return false;
                }
            }
        };

        return findFiles(inUrls, fileNamePredicate);
    }

    /** return an iterable of all {@link File} in given urls, matching filePredicate */
    public static Iterable<File> findFiles(final Collection<URL> inUrls, final Predicate<File> filePredicate) {
        Iterable<File> result = new ArrayList<File>();

        for (final URL url : inUrls) {
            try {
                result = Iterables.concat(result,
                        Iterables.filter(new Iterable<File>() {
                            public Iterator<File> iterator() {
                                return fromURL(url).getFiles().iterator();
                            }
                        }, filePredicate));
            } catch (Throwable e) {
                if (Reflections.log != null) {
                    Reflections.log.error("could not findFiles for url. continuing. [" + url + "]", e);
                }
            }
        }

        return result;
    }

    /**try to get {@link java.io.File} from url*/
    public static @Nullable java.io.File getFile(URL url) {
        java.io.File file;
        String path;

        try {
            path = url.toURI().getSchemeSpecificPart();
            if ((file = new java.io.File(path)).exists()) return file;
        } catch (URISyntaxException e) {
        }

        try {
            path = URLDecoder.decode(url.getPath(), "UTF-8");
            if (path.contains(".jar!")) path = path.substring(0, path.lastIndexOf(".jar!") + ".jar".length());
            if ((file = new java.io.File(path)).exists()) return file;

        } catch (UnsupportedEncodingException e) {
        }

        try {
            // 类型还挺多的
            path = url.toExternalForm();
            if (path.startsWith("jar:")) path = path.substring("jar:".length());
            if (path.startsWith("wsjar:")) path = path.substring("wsjar:".length());
            if (path.startsWith("file:")) path = path.substring("file:".length());
            if (path.contains(".jar!")) path = path.substring(0, path.indexOf(".jar!") + ".jar".length());
            if (path.contains(".war!")) path = path.substring(0, path.indexOf(".war!") + ".war".length());
            if ((file = new java.io.File(path)).exists()) return file;

            path = path.replace("%20", " ");
            if ((file = new java.io.File(path)).exists()) return file;

        } catch (Exception e) {
        }

        return null;
    }
    
    private static boolean hasJarFileInPath(URL url) {
		return url.toExternalForm().matches(".*\\.jar(\\!.*|$)");
	}

    /** default url types used by {@link Vfs#fromURL(URL)}
     * <p>
     * <p>jarFile - creates a {@link org.reflections.vfs.ZipDir} over jar file
     * <p>jarUrl - creates a {@link org.reflections.vfs.ZipDir} over a jar url (contains ".jar!/" in it's name), using Java's {@link JarURLConnection}
     * <p>directory - creates a {@link org.reflections.vfs.SystemDir} over a file system directory
     * <p>jboss vfs - for protocols vfs, using jboss vfs (should be provided in classpath)
     * <p>jboss vfsfile - creates a {@link UrlTypeVFS} for protocols vfszip and vfsfile.
     * <p>bundle - for bundle protocol, using eclipse FileLocator (should be provided in classpath)
     * <p>jarInputStream - creates a {@link JarInputDir} over jar files, using Java's JarInputStream
     * */
    // 【感觉这个还挺有用的】
    public static enum DefaultUrlTypes implements UrlType {

        /** jar 文件 */
        jarFile {
            @Override
            public boolean matches(URL url) {
                return url.getProtocol().equals("file") && hasJarFileInPath(url);
            }

            @Override
            public Dir createDir(final URL url) throws Exception {
                return new ZipDir(new JarFile(getFile(url)));
            }
        },

        /** jar url 没注意到这个url协议*/
        jarUrl {
            @Override
            public boolean matches(URL url) {
                return "jar".equals(url.getProtocol()) || "zip".equals(url.getProtocol()) || "wsjar".equals(url.getProtocol());
            }

            @Override
            public Dir createDir(URL url) throws Exception {
                try {
                    URLConnection urlConnection = url.openConnection();
                    if (urlConnection instanceof JarURLConnection) {
                        urlConnection.setUseCaches(false);
                        return new ZipDir(((JarURLConnection) urlConnection).getJarFile());
                    }
                } catch (Throwable e) { /*fallback*/ }
                java.io.File file = getFile(url);
                if (file != null) {
                    return new ZipDir(new JarFile(file));
                }
                return null;
            }
        },

        /** 目录 */
        directory {
            @Override
            public boolean matches(URL url) {
                if (url.getProtocol().equals("file") && !hasJarFileInPath(url)) {
                    java.io.File file = getFile(url);
                    return file != null && file.isDirectory();
                } else return false;
            }

            @Override
            public Dir createDir(final URL url) throws Exception {
                return new SystemDir(getFile(url));
            }
        },

        /** 这是啥？ */
        jboss_vfs {
            @Override
            public boolean matches(URL url) {
                return url.getProtocol().equals("vfs");
            }

            @Override
            public Dir createDir(URL url) throws Exception {
                Object content = url.openConnection().getContent();
                Class<?> virtualFile = ClasspathHelper.contextClassLoader().loadClass("org.jboss.vfs.VirtualFile");
                java.io.File physicalFile = (java.io.File) virtualFile.getMethod("getPhysicalFile").invoke(content);
                String name = (String) virtualFile.getMethod("getName").invoke(content);
                java.io.File file = new java.io.File(physicalFile.getParentFile(), name);
                if (!file.exists() || !file.canRead()) file = physicalFile;
                return file.isDirectory() ? new SystemDir(file) : new ZipDir(new JarFile(file));
            }
        },

        /** 这又是啥？ */
        jboss_vfsfile {
            @Override
            public boolean matches(URL url) throws Exception {
                return "vfszip".equals(url.getProtocol()) || "vfsfile".equals(url.getProtocol());
            }

            @Override
            public Dir createDir(URL url) throws Exception {
                return new UrlTypeVFS().createDir(url);
            }
        },

        /** java中的 bundle模块？ */
        bundle {
            @Override
            public boolean matches(URL url) throws Exception {
                return url.getProtocol().startsWith("bundle");
            }

            @Override
            public Dir createDir(URL url) throws Exception {
                return fromURL((URL) ClasspathHelper.contextClassLoader().
                        loadClass("org.eclipse.core.runtime.FileLocator").getMethod("resolve", URL.class).invoke(null, url));
            }
        },

        jarInputStream {
            @Override
            public boolean matches(URL url) throws Exception {
                return url.toExternalForm().contains(".jar");
            }

            @Override
            public Dir createDir(final URL url) throws Exception {
                return new JarInputDir(url);
            }
        }
    }
}

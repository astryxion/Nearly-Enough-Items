package codechicken.nei.bridge;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.api.IConfigureNEI;

/**
 * Discovers {@code NEI*Config} classes the same way classic 1.7.10 NEI does
 * (class name prefix/suffix) and runs {@link IConfigureNEI#loadConfig()}.
 */
public final class NeiPluginLoader {
	private static ASMDataTable asmData;
	private static boolean loaded;

	private NeiPluginLoader() {
	}

	public static void captureAsm(FMLPreInitializationEvent event) {
		asmData = event.getAsmData();
	}

	public static void load() {
		if (loaded) {
			return;
		}
		loaded = true;
		List<Class<?>> plugins = new ArrayList<Class<?>>();
		findFromModSources(plugins);
		findFromAsm(plugins);

		NeiCompatLog.info("Discovered {} IConfigureNEI plugin(s)", Integer.valueOf(plugins.size()));
		for (int i = 0; i < plugins.size(); i++) {
			Class<?> clazz = plugins.get(i);
			try {
				Object instance = clazz.newInstance();
				if (!(instance instanceof IConfigureNEI)) {
					continue;
				}
				IConfigureNEI config = (IConfigureNEI) instance;
				NeiCompatLog.info("Loading NEI plugin {} {} ({})", config.getName(), config.getVersion(), clazz.getName());
				config.loadConfig();
				NeiCompatLog.info("Loaded NEI plugin {}", clazz.getName());
			} catch (Throwable t) {
				NeiCompatLog.error("Failed to load NEI plugin {}", clazz.getName(), t);
			}
		}
		NEIClientConfig.loaded = true;
	}

	private static void findFromAsm(List<Class<?>> plugins) {
		if (asmData == null) {
			return;
		}
		try {
			for (ASMDataTable.ASMData data : asmData.getAll(IConfigureNEI.class.getName().replace('.', '/'))) {
				addClass(plugins, data.getClassName().replace('/', '.'));
			}
			for (ASMDataTable.ASMData data : asmData.getAll(IConfigureNEI.class.getName())) {
				addClass(plugins, data.getClassName().replace('/', '.'));
			}
		} catch (Throwable t) {
			NeiCompatLog.warn("ASM plugin scan failed", t);
		}
	}

	private static void findFromModSources(List<Class<?>> plugins) {
		List<ModContainer> mods = Loader.instance().getActiveModList();
		for (int i = 0; i < mods.size(); i++) {
			ModContainer mod = mods.get(i);
			File source = mod.getSource();
			if (source == null || !source.exists()) {
				continue;
			}
			try {
				if (source.isDirectory()) {
					scanDirectory(source, source, plugins);
				} else {
					scanZip(source, plugins);
				}
			} catch (Throwable t) {
				NeiCompatLog.warn("Failed scanning mod source {} for NEI plugins", source, t);
			}
		}
	}

	private static void scanZip(File zipFile, List<Class<?>> plugins) throws Exception {
		ZipFile zip = new ZipFile(zipFile);
		try {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();
				if (isNeiConfigClassFile(name)) {
					addClass(plugins, name.substring(0, name.length() - 6).replace('/', '.'));
				}
			}
		} finally {
			zip.close();
		}
	}

	private static void scanDirectory(File root, File current, List<Class<?>> plugins) {
		File[] children = current.listFiles();
		if (children == null) {
			return;
		}
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			if (child.isDirectory()) {
				scanDirectory(root, child, plugins);
			} else if (isNeiConfigClassFile(relativeClassPath(root, child))) {
				addClass(plugins, relativeClassPath(root, child).substring(0, relativeClassPath(root, child).length() - 6).replace('/', '.').replace('\\', '.'));
			}
		}
	}

	private static String relativeClassPath(File root, File file) {
		String rootPath = root.getAbsolutePath();
		String filePath = file.getAbsolutePath();
		if (filePath.startsWith(rootPath)) {
			filePath = filePath.substring(rootPath.length());
		}
		if (filePath.startsWith(File.separator) || filePath.startsWith("/") || filePath.startsWith("\\")) {
			filePath = filePath.substring(1);
		}
		return filePath.replace(File.separatorChar, '/');
	}

	private static boolean isNeiConfigClassFile(String path) {
		if (path == null || !path.endsWith("Config.class")) {
			return false;
		}
		int slash = path.lastIndexOf('/');
		String simple = slash < 0 ? path : path.substring(slash + 1);
		return simple.startsWith("NEI") && simple.endsWith("Config.class");
	}

	private static void addClass(List<Class<?>> plugins, String className) {
		if (className == null) {
			return;
		}
		try {
			Class<?> clazz = Class.forName(className, true, Loader.instance().getModClassLoader());
			if (!IConfigureNEI.class.isAssignableFrom(clazz) || clazz.isInterface()) {
				return;
			}
			for (int i = 0; i < plugins.size(); i++) {
				if (plugins.get(i) == clazz) {
					return;
				}
			}
			plugins.add(clazz);
		} catch (Throwable t) {
			NeiCompatLog.warn("Could not load candidate NEI plugin class {}", className);
		}
	}
}

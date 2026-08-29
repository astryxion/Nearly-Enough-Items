package codechicken.nei.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.bridge.NeiCompatLog;

/**
 * NEI data-dump option base. MutationDumper and similar addons subclass this
 * through {@link ArrayDumper}.
 */
public abstract class DataDumper extends Option {

	public DataDumper(String name) {
		super(name);
		NeiCompatLog.api("DataDumper", name);
	}

	public abstract String[] header();

	public abstract Iterable<String[]> dump(int mode);

	public String renderName() {
		return translateN(name + "s");
	}

	public void dumpFile() {
		try {
			File dumps = new File(Minecraft.getMinecraft().mcDataDir, "dumps");
			if (!dumps.exists()) {
				dumps.mkdirs();
			}
			File file = new File(dumps, getFileName(name.replaceFirst(".+\\.", "")));
			if (!file.exists()) {
				file.createNewFile();
			}
			dumpTo(file);
			NEIClientUtils.printChatMessage(dumpMessage(file));
		} catch (Exception e) {
			NEIClientConfig.logger.error("Error dumping " + renderName() + " mode: " + getMode(), e);
		}
	}

	public String getFileName(String prefix) {
		return prefix + getFileExtension();
	}

	public String getFileExtension() {
		return ".csv";
	}

	public IChatComponent dumpMessage(File file) {
		return new ChatComponentTranslation("nei.options.tools.dump.dumped", translateN(name), "dumps/" + file.getName());
	}

	public void dumpTo(File file) throws IOException {
		int mode = getMode();
		PrintWriter writer = new PrintWriter(file);
		writer.println(concat(header()));
		for (String[] line : dump(mode)) {
			writer.println(concat(line));
		}
		writer.close();
	}

	public static String concat(String[] header) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < header.length; i++) {
			String s = header[i];
			if (sb.length() > 0) {
				sb.append(',');
			}
			if (s == null) {
				s = "null";
			}
			if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0) {
				s = '"' + s.replace("\"", "\"\"") + '"';
			}
			sb.append(s);
		}
		return sb.toString();
	}

	public int getMode() {
		return renderTag().getIntValue(0);
	}

	public int modeCount() {
		return 3;
	}
}

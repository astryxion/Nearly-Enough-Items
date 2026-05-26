package astryxion.nei;

import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import astryxion.nei.api.INbtIgnoreList;
import astryxion.nei.config.Config;
import astryxion.nei.util.Log;

public class NbtIgnoreList implements INbtIgnoreList {
	private final Set<String> nbtTagNameBlacklist = new HashSet<>();

	@Override
	public void ignoreNbtTagNames(String... nbtTagNames) {
		Collections.addAll(nbtTagNameBlacklist, nbtTagNames);
	}

	@Override
	public boolean isNbtTagIgnored(@Nullable String nbtTagName) {
		if (nbtTagName == null) {
			Log.error("Null nbtTagName", new NullPointerException());
			return false;
		}
		return Config.getNbtKeyIgnoreList().contains(nbtTagName) || nbtTagNameBlacklist.contains(nbtTagName);
	}

	@Override
	@Nonnull
	public Set<String> getIgnoredNbtTags(@Nullable Set<String> nbtTagNames) {
		if (nbtTagNames == null) {
			Log.error("Null nbtTagNames", new NullPointerException());
			return Collections.emptySet();
		}
		Set<String> ignoredKeysConfig = Sets.intersection(nbtTagNames, Config.getNbtKeyIgnoreList());
		Set<String> ignoredKeysApi = Sets.intersection(nbtTagNames, nbtTagNameBlacklist);
		return Sets.union(ignoredKeysConfig, ignoredKeysApi);
	}
}

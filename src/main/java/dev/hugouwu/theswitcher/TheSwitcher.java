package dev.hugouwu.theswitcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheSwitcher {
	private static final @Nullable TheSwitcher instance = new TheSwitcher();
	private static final @NotNull String VERSION = dev.hugouwu.theswitcher.BuildConstants.VERSION;
	public static final Map<UUID, ResourceLocation> SKIN_CACHE = new HashMap<>();
	public TheSwitcher() {
	}

	public static @NotNull TheSwitcher get() {
		return Objects.requireNonNull(instance);
	}

	public @NotNull String getVersion() {
		return VERSION;
	}
}

package dev.hugouwu.theswitcher.account;

import dev.hugouwu.theswitcher.utils.Shared;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Microsoft account for Minecraft.
 *
 * @author VidTu
 */
public class MicrosoftAccount implements Account {
	private String name;
	private String accessToken;
	private String refreshToken;
	private UUID uuid;
	public MicrosoftAccount(@NotNull String name, @NotNull String accessToken,
			@NotNull String refreshToken, @NotNull UUID uuid) {
		this.name = name;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.uuid = uuid;
	}

	@Override
	public @NotNull UUID uuid() {
		return uuid;
	}

	@Override
	public @NotNull String name() {
		return name;
	}

	/**
	 * Get access token of this account.
	 *
	 * @return Access token
	 */
	@Contract(pure = true)
	public @NotNull String accessToken() {
		return accessToken;
	}

	/**
	 * Get refresh token of this account.
	 *
	 * @return Refresh token
	 */
	@Contract(pure = true)
	public String refreshToken() {
		return refreshToken;
	}

	@Override
	public @NotNull CompletableFuture<@NotNull AuthData> login(
			@NotNull BiConsumer<@NotNull String, @NotNull Object[]> progressHandler) {
		CompletableFuture<AuthData> cf = new CompletableFuture<>();
		Shared.EXECUTOR.execute(() -> {
			try {
				refresh(progressHandler);
				cf.complete(new AuthData(name, uuid, accessToken, AuthData.MSA));
			} catch (Throwable t) {
				Shared.LOG.error("Unable to login/refresh Microsoft account.", t);
				cf.completeExceptionally(t);
			}
		});
		return cf;
	}

	/**
	 * Validate and refresh account.
	 *
	 * @throws Exception
	 *             If something goes wrong
	 */
	private void refresh(@NotNull BiConsumer<@NotNull String, @NotNull Object[]> progressHandler)
			throws Exception {
		try {
			Shared.LOG.info("Refreshing...");
			progressHandler.accept("ias.loginGui.microsoft.progress", new Object[]{"getProfile"});
			Map.Entry<UUID, String> profile = Auth.getProfile(accessToken);
			Shared.LOG.info("Access token is valid.");
			uuid = profile.getKey();
			name = profile.getValue();
		} catch (Exception e) {
			try {
				Shared.LOG.info("Step: refreshToken.");
				progressHandler.accept("ias.loginGui.microsoft.progress",
						new Object[]{"refreshToken"});
				Map.Entry<String, String> authRefreshTokens = Auth.refreshToken(refreshToken);
				String refreshToken = authRefreshTokens.getValue();
				Shared.LOG.info("Step: authXBL.");
				progressHandler.accept("ias.loginGui.microsoft.progress", new Object[]{"authXBL"});
				String xblToken = Auth.authXBL(authRefreshTokens.getKey());
				Shared.LOG.info("Step: authXSTS.");
				progressHandler.accept("ias.loginGui.microsoft.progress", new Object[]{"authXSTS"});
				Map.Entry<String, String> xstsTokenUserhash = Auth.authXSTS(xblToken);
				Shared.LOG.info("Step: authMinecraft.");
				progressHandler.accept("ias.loginGui.microsoft.progress",
						new Object[]{"authMinecraft"});
				String accessToken = Auth.authMinecraft(xstsTokenUserhash.getValue(),
						xstsTokenUserhash.getKey());
				Shared.LOG.info("Step: getProfile.");
				progressHandler.accept("ias.loginGui.microsoft.progress",
						new Object[]{"getProfile"});
				Map.Entry<UUID, String> profile = Auth.getProfile(accessToken);
				Shared.LOG.info("Refreshed.");
				this.uuid = profile.getKey();
				this.name = profile.getValue();
				this.accessToken = accessToken;
				this.refreshToken = refreshToken;
			} catch (Exception ex) {
				ex.addSuppressed(e);
				throw ex;
			}
		}
	}
}

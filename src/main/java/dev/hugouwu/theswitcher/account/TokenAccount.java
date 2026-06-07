package dev.hugouwu.theswitcher.account;

import dev.hugouwu.theswitcher.utils.Shared;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TokenAccount implements Account {
    private String name;
    private String accessToken;
    private UUID uuid;

    public TokenAccount(@NotNull String accessToken) throws Exception {
        Map.Entry<UUID, String> profile = Auth.getProfile(accessToken);
        this.uuid = profile.getKey();
        this.name = profile.getValue();
        this.accessToken = accessToken;
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

    @Override
    public @NotNull CompletableFuture<@NotNull AuthData> login(
            @NotNull BiConsumer<@NotNull String, @NotNull Object[]> progressHandler) {
        CompletableFuture<AuthData> cf = new CompletableFuture<>();
        Shared.EXECUTOR.execute(() -> {
            try {
                // Validate access token and refresh profile data (no refresh token used)
                progressHandler.accept("ias.loginGui.microsoft.progress",
                        new Object[]{"getProfile"});
                Map.Entry<UUID, String> profile = Auth.getProfile(accessToken);
                this.uuid = profile.getKey();
                this.name = profile.getValue();
                cf.complete(new AuthData(name, uuid, accessToken, AuthData.MSA));
            } catch (Throwable t) {
                Shared.LOG.error("Unable to login with Microsoft access token.", t);
                cf.completeExceptionally(t);
            }
        });
        return cf;
    }
}
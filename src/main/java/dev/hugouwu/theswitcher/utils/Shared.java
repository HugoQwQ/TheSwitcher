package dev.hugouwu.theswitcher.utils;

import com.google.gson.Gson;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Shared {
	public static final Executor EXECUTOR = Executors
			.newSingleThreadExecutor(r -> new Thread(r, "IAS Executor"));
	public static final Logger LOG = LogManager.getLogger("IAS");
	public static final Gson GSON = new Gson();
}

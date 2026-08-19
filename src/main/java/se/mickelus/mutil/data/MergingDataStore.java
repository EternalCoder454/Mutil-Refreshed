package se.mickelus.mutil.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MergingDataStore<V, U> extends DataStore<V> {
    private static final Logger logger = LogManager.getLogger();

    protected Class<U> arrayClass;

    public MergingDataStore(Gson gson, String namespace, String directory, Class<V> entryClass, Class<U> arrayClass, DataDistributor synchronizer) {
        super(gson, namespace, directory, entryClass, synchronizer);

        this.arrayClass = arrayClass;
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        logger.debug("Reading data for {} data store...", directory);
        Map<Identifier, JsonElement> map = Maps.newHashMap();
        int i = this.directory.length() + 1;

        for (Map.Entry<Identifier, List<Resource>> entry : resourceManager.listResourceStacks(directory, rl -> rl.getPath().endsWith(".json")).entrySet()) {
            if (!namespace.equals(entry.getKey().getNamespace())) {
                continue;
            }

            String path = entry.getKey().getPath();
            Identifier location = Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(), path.substring(i, path.length() - jsonExtLength));

            JsonArray allResources = new JsonArray();

            for (Resource resource : entry.getValue()) {
                try (Reader reader = resource.openAsReader()) {
                    JsonObject json = GsonHelper.fromJson(gson, reader, JsonObject.class);
                    json.add("sources", getSources(resource));

                    if (json != null) {
                        if (shouldLoad(json)) {
                            allResources.add(json);
                        } else {
                            logger.debug("Skipping data '{}' from '{}' due to condition", entry.getKey(), resource.sourcePackId());
                        }
                    } else {
                        logger.error("Couldn't load data from '{}' in data pack '{}' as it's empty or null",
                                entry.getKey(), resource.sourcePackId());
                    }
                } catch (RuntimeException | IOException e) {
                    logger.error("Couldn't load data from '{}' in data pack '{}'", entry.getKey(), resource.sourcePackId(), e);
                }
            }

            if (allResources.size() > 0) {
                map.put(location, allResources);
            }
        }

        return map;
    }

    @Override
    public void loadFromPacket(Map<Identifier, String> data) {
        parseData(readPacket(data, entry -> GsonHelper.fromJson(gson, entry, JsonArray.class)));
    }

    /**
      * Parse and merge every entry, dropping any that cannot be read.
      *
      * <p>This is the store behind modules, schematics, materials and crafting effects, which are
      * the four an addon is most likely to extend and the four with the most files. It used to
      * collect them in one stream, so one unreadable entry threw out of the collect and left the
      * store empty, taking every module or every schematic in the game with it.
      */
    @Override
    public void parseData(Map<Identifier, JsonElement> splashList) {
        logger.info("Loaded {} {}", String.format("%3d", splashList.values().size()), directory);

        Map<Identifier, V> parsed = new HashMap<>();
        List<Identifier> failed = new ArrayList<>();
        for (Map.Entry<Identifier, JsonElement> entry : splashList.entrySet()) {
            try {
                parsed.put(entry.getKey(), mergeData(gson.fromJson(entry.getValue(), arrayClass)));
            } catch (RuntimeException e) {
                failed.add(entry.getKey());
                logger.error("Dropping '{}' from {}, it could not be parsed: {}",
                        entry.getKey(), directory, e.getMessage());
            }
        }

        if (!failed.isEmpty()) {
            logger.error("{} of {} files in {} were dropped: {}", failed.size(),
                    splashList.size(), directory, failed);
        }

        dataMap = parsed;

        processData();

        notifyListeners();
    }

    protected abstract V mergeData(U collection);
}

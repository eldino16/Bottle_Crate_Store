package de.uniba.dsg.cloudfunction;

import java.util.Date;
import java.util.Map;

public class Event {
    private String bucket;
    private String name;
    private String metageneration;
    private Date timeCreated;
    private Date updated;
    private Map<String, String> metadata;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetageneration() {
        return metageneration;
    }

    public void setMetageneration(String metageneration) {
        this.metageneration = metageneration;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}

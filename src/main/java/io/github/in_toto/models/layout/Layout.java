package io.github.in_toto.models.layout;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.Key.SetKeyJsonAdapter;
import io.github.in_toto.models.Signable;
import io.github.in_toto.models.SignableType;

public final class Layout implements Signable {

    private final String name;
    @SerializedName("_type")
    private final SignableType type = SignableType.layout;
    private final Set<Step> steps;
    @SerializedName("inspect")
    private final Set <Inspection> inspections;
    @JsonAdapter(SetKeyJsonAdapter.class)
    private final Set<Key> keys;
    @JsonAdapter(ZonedDateTimeJsonAdapter.class)
    private final ZonedDateTime expires;
    private final String readme;

    public Layout(String name, Set<Step> steps, Set<Inspection> inspections, Set<Key> keys,
            ZonedDateTime expires, String readme) {
        this.name = name;
        if (steps != null) {
            this.steps = Collections.unmodifiableSet(new HashSet<>(steps));
        } else {
            this.steps = Collections.unmodifiableSet(new HashSet<>()); 
        }
        if (inspections != null) {
            this.inspections = Collections.unmodifiableSet(new HashSet<>(inspections)); 
        } else {
            this.inspections = Collections.unmodifiableSet(new HashSet<>());
        }
        if (keys != null) {
            this.keys = Collections.unmodifiableSet(new HashSet<>(keys)); 
        } else {
            this.keys = Collections.unmodifiableSet(new HashSet<>());
        }
        this.expires = expires;
        this.readme = readme;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public SignableType getType() {
        return type;
    }

    @Override
    public String getFullName(String shortKey) {
        return this.name+"."+type;
    }

    public Set<Step> getSteps() {
        return steps;
    }

    public Set<Key> getKeys() {
        return keys;
    }

    public ZonedDateTime getExpires() {
        return expires;
    }

    public String getReadme() {
        return readme;
    }
    
    public Set<Inspection> getInspections() {
        return inspections;
    }

    public boolean isExpired() {
        return !(this.expires == null || this.expires.compareTo(ZonedDateTime.now(ZoneId.of("GMT"))) >= 0);
    }

    @Override
    public String toString() {
        return "Layout [name=" + name + ", steps=" + steps + ", inspections=" + inspections + ", keys=" + keys
                + ", expires=" + expires + ", readme=" + readme + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expires == null) ? 0 : expires.hashCode());
        result = prime * result + ((inspections == null) ? 0 : inspections.hashCode());
        result = prime * result + ((keys == null) ? 0 : keys.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((readme == null) ? 0 : readme.hashCode());
        result = prime * result + ((steps == null) ? 0 : steps.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Layout other = (Layout) obj;
        if (expires == null) {
            if (other.expires != null) {
                return false;
            }
        } else if (!expires.equals(other.expires)) {
            return false;
        }
        if (inspections == null) {
            if (other.inspections != null) {
                return false;
            }
        } else if (!inspections.equals(other.inspections)) {
            return false;
        }
        if (keys == null) {
            if (other.keys != null) {
                return false;
            }
        } else if (!keys.equals(other.keys)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (readme == null) {
            if (other.readme != null) {
                return false;
            }
        } else if (!readme.equals(other.readme)) {
            return false;
        }
        if (steps == null) {
            if (other.steps != null) {
                return false;
            }
        } else if (!steps.equals(other.steps)) {
            return false;
        }
        return type == other.type;
    }

}

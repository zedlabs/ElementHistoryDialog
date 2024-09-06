package me.zed.elementhistorydialog.elements;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * members of a relation, i.e associated OSM elements
 */
public class RelationMember implements Serializable {
    private static final long serialVersionUID = 6L;

    final String type;
    long ref;
    String role = null;

    /**
     * Constructor for members that have not been downloaded
     *
     * @param t  type of the member OsmElement
     * @param id the OSM id
     * @param r  the role of the element
     */
    public RelationMember(@NonNull final String t, final long id, @Nullable final String r) {
        type = t;
        ref = id;
        role = r;
    }

    /**
     * Get the OsmElement type
     *
     * @return the type (NODE, WAY, RELATION) as a String
     */
    @NonNull
    public String getType() {
        return type;
    }

    /**
     * Get the OSM id of the element
     *
     * @return the OSM id
     */
    public long getRef() {
        return ref;
    }

    /**
     * Get the role of this relation member
     *
     * @return the role or null if not set
     */
    @Nullable
    public String getRole() {
        return role;
    }

    /**
     * Set the role for the element
     *
     * @param role the new role to set
     */
    public void setRole(@Nullable final String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role + " " + type + " " + ref;
    }
}

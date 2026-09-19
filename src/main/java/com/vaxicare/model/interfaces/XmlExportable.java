package com.vaxicare.model.interfaces;

/**
 * Interface allowing an entity to be serialized into structured XML.
 * Demonstrates: Interface contracts for XML interoperability in Java.
 */
public interface XmlExportable {
    /**
     * Converts domain entity properties into a valid XML fragment string.
     * @return Formatted XML representation.
     */
    String toXmlFragment();
}

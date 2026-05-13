/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.util.UUID;

public final class Id implements java.io.Serializable {
    @java.io.Serial
    private static final long serialVersionUID = -5034902813971242584L;

    private final String value;

    public Id(String value) {
        this.value = value;
    }

    public static Id genNew() {
        return new Id(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Id other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

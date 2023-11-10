package net.blusalt.posplugin.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Objects;

@Parcel
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor

public class BtDevice {

    @Expose
    @SerializedName("name")
    String  name;

    @Expose
    @SerializedName("address")
    String  address;

    public BtDevice(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public BtDevice() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BtDevice btDevice = (BtDevice) o;
        return Objects.equals(name, btDevice.name) && Objects.equals(address, btDevice.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }
}


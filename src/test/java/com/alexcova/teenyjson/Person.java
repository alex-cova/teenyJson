package com.alexcova.teenyjson;

import com.alexcova.teenyjson.api.JsonAlias;
import com.alexcova.teenyjson.api.JsonIncludeNonNull;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@JsonIncludeNonNull
public class Person {
    private String name;
    private int age;
    private LocalDate birthDate;
    private LocalDateTime lastUpdated;
    @SerializedName("IceCreamLover")
    private boolean likesIceCream;
    private Pet pet;
    @SerializedName("bestSongs")
    private Set<String> favoriteSongs;
    private List<Tool> toolList;

    public List<Tool> getToolList() {
        return toolList;
    }

    public Person setToolList(List<Tool> toolList) {
        this.toolList = toolList;
        return this;
    }

    public Person setFavoriteSongs(Set<String> favoriteSongs) {
        this.favoriteSongs = favoriteSongs;
        return this;
    }

    @JsonAlias("bestSongs")
    public Set<String> getFavoriteSongs() {
        return favoriteSongs;
    }

    public Person setPet(Pet pet) {
        this.pet = pet;
        return this;
    }

    public Pet getPet() {
        return pet;
    }

    public String getName() {
        return name;
    }

    public Person setName(String name) {
        this.name = name;
        return this;
    }

    public int getAge() {
        return age;
    }

    public Person setAge(int age) {
        this.age = age;
        return this;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Person setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
        return this;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public Person setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    @JsonAlias("IceCreamLover")
    public boolean isLikesIceCream() {
        return likesIceCream;
    }

    public Person setLikesIceCream(boolean likesIceCream) {
        this.likesIceCream = likesIceCream;
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Person person = (Person) object;

        if (age != person.age) return false;
        if (likesIceCream != person.likesIceCream) return false;
        if (!Objects.equals(name, person.name)) return false;
        if (!Objects.equals(birthDate, person.birthDate)) return false;
        if (!Objects.equals(lastUpdated, person.lastUpdated))
            return false;
        if (!Objects.equals(pet, person.pet)) return false;
        if (!Objects.equals(favoriteSongs, person.favoriteSongs))
            return false;
        return Objects.equals(toolList, person.toolList);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + age;
        result = 31 * result + (birthDate != null ? birthDate.hashCode() : 0);
        result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
        result = 31 * result + (likesIceCream ? 1 : 0);
        result = 31 * result + (pet != null ? pet.hashCode() : 0);
        result = 31 * result + (favoriteSongs != null ? favoriteSongs.hashCode() : 0);
        result = 31 * result + (toolList != null ? toolList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", birthDate=" + birthDate +
                ", lastUpdated=" + lastUpdated +
                ", likesIceCream=" + likesIceCream +
                ", pet=" + pet +
                ", favoriteSongs=" + favoriteSongs +
                ", toolList=" + toolList +
                '}';
    }
}
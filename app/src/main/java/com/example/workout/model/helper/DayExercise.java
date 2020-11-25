package com.example.workout.model.helper;

public class DayExercise {
    private int id;
    private int typeId;
    private String name;
    private int dataType;

    public DayExercise(int typeId, String name, int dataType) {
        this.typeId = typeId;
        this.name = name;
        this.dataType = dataType;
    }

    public DayExercise(int id, int typeId, String name, int dataType) {
        this.id = id;
        this.typeId = typeId;
        this.name = name;
        this.dataType = dataType;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    /** Returns id of the connection */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** Returns id of the element */
    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
}

package com.arabic.math.solver;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Classification {

    @SerializedName("equation")
    @Expose
    private String equation;
    @SerializedName("mapping")
    @Expose
    private String mapping;
    @SerializedName("solution")
    @Expose
    private String solution;

    public String getEquation() {
        return equation == null ?"":equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }

    public String getMapping() {
        return mapping == null ?"":mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getSolution() {
        return solution== null ?"":solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

}
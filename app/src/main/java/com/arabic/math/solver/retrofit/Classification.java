package com.arabic.math.solver.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Classification {

    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("expression")
    @Expose
    private String expression;
    @SerializedName("mapping")
    @Expose
    private String mapping;
    @SerializedName("solution")
    @Expose
    private String solution;

    public String getEquation() {
        return expression == null ?"":expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
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

    public String getError() {
        return error== null ?"None":error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
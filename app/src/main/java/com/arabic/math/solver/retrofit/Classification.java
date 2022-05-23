package com.arabic.math.solver.retrofit;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Classification {

    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("expression")
    @Expose
    private String expression;
    @SerializedName("solution")
    @Expose
    private ArrayList<String> solution;

    @NonNull
    public String getEquation() {
        return expression == null ? "" : expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @NonNull
    public ArrayList<String> getSolution() {
        return solution == null ? new ArrayList<>() : solution;
    }

    public void setSolution(ArrayList<String> solution) {
        this.solution = solution;
    }

    @NonNull
    public String getError() {
        return error == null ? "None" : error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean containsNull() {
        return solution == null || expression == null || error == null;
    }
}
package com.arabic.math.solver.retrofit;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableStringBuilder;

import androidx.annotation.NonNull;

import com.arabic.math.solver.CustomTypefaceSpan;
import com.arabic.math.solver.R;
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

    public SpannableStringBuilder buildResponeStr(Resources res, Typeface font, int HtmlFlag) {
        SpannableStringBuilder solution_str = new SpannableStringBuilder(), builder = new SpannableStringBuilder();
        for (int i = 0; i < this.getSolution().size(); i++) {
            solution_str.append(Html.fromHtml(this.getSolution().get(i), HtmlFlag));
            if (i + 1 != this.getSolution().size())
                solution_str.append(" , ");
        }
        builder.append(res.getString(R.string.equation_str)).append(" : ").append(Html.fromHtml(this.getEquation(), HtmlFlag), new CustomTypefaceSpan(font), 0);
        if (!this.getSolution().isEmpty())
            builder.append("\n").append(res.getString(R.string.solution_str)).append(" : ").append(solution_str, new CustomTypefaceSpan(font), 0);
        if (!this.getError().isEmpty())
            builder.append("\n").append(res.getString(R.string.error_str)).append(" : ").append(this.getError());
        return builder;
    }
}
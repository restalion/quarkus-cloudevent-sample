package com.accenture.sample.cloudevent.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SampleDTO {

    @NotNull(message = "Code value should be not null")
    private Integer code;

    @Size(min = 10, max = 200, message = "Value should be between 10 and 200 characters")
    private String value;

    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
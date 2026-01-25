package com.banbi.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@AllArgsConstructor
@Data
public class HelloObject implements Serializable {
    private Integer id;
    private String message;
}

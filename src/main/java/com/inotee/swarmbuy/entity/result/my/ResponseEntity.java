package com.inotee.swarmbuy.entity.result.my;
import lombok.*;

import java.io.Serializable;
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseEntity<T> implements Serializable {
    private Boolean success;
    private String resultCode;
    private String resulteMessage;
    private T result;
}

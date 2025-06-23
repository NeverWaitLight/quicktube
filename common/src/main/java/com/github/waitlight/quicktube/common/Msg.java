package com.github.waitlight.quicktube.common;

import java.io.Serializable;

public record Msg(
        Adr adr,
        String content
) implements Serializable {

}

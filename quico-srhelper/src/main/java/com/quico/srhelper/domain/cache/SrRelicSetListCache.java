package com.quico.srhelper.domain.cache;

import com.quico.srhelper.domain.SrRelicSet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrRelicSetListCache {
    private static final long serialVersionUID = 1L;

    private List<SrRelicSet> list;
}

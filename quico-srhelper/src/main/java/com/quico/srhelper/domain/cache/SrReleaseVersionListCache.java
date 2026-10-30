package com.quico.srhelper.domain.cache;

import com.quico.srhelper.domain.SrReleaseVersion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SrReleaseVersionListCache {
    private static final long serialVersionUID = 1L;

    private List<SrReleaseVersion> list;
}

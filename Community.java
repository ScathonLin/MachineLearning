package com.huawei.scathon.aiops.louvain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString
@Builder
public class Community {
    @Getter @Setter private int comId;
    @Getter @Setter private int sigmaIn;
    @Getter @Setter private int sigmaTot;
    @Getter @Setter private int ki;

    private Community(int comId) {
        this.comId = comId;
    }

    @Override
    public int hashCode() {
        return (String.valueOf(comId) + "*#*#").hashCode();
    }

    @Override
    public boolean equals(Object targetObj) {
        boolean flag = false;
        if (targetObj instanceof Community) {
            if (comId == ((Community) targetObj).getComId()) {
                flag = true;
            }
        }
        return flag;
    }

}

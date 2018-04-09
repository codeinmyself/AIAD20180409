package com.xmu.lxq.aiad.gpufilter.helper;


import com.xmu.lxq.aiad.gpufilter.basefilter.GPUImageFilter;
import com.xmu.lxq.aiad.gpufilter.filter.MagicAntiqueFilter;
import com.xmu.lxq.aiad.gpufilter.filter.MagicBrannanFilter;
import com.xmu.lxq.aiad.gpufilter.filter.MagicCoolFilter;
import com.xmu.lxq.aiad.gpufilter.filter.MagicFreudFilter;
import com.xmu.lxq.aiad.gpufilter.filter.MagicHefeFilter;
import com.xmu.lxq.aiad.gpufilter.filter.MagicHudsonFilter;
import com.xmu.lxq.aiad.gpufilter.filter.MagicInkwellFilter;
import com.xmu.lxq.aiad.gpufilter.filter.MagicN1977Filter;
import com.xmu.lxq.aiad.gpufilter.filter.MagicNashvilleFilter;

public class MagicFilterFactory {

    private static MagicFilterType filterType = MagicFilterType.正常;

    public static GPUImageFilter initFilters(MagicFilterType type) {
        if (type == null) {
            return null;
        }
        filterType = type;
        switch (type) {
            case 罗马古韵:
                return new MagicAntiqueFilter();
            case 悠闲村庄:
                return new MagicBrannanFilter();
            case 旋转的糯米糍:
                return new MagicFreudFilter();
            case 冷冽冰霜:
                return new MagicHefeFilter();
            case 伏特加之舞:
                return new MagicHudsonFilter();
            case 山水之间:
                return new MagicInkwellFilter();
            case 时间巨轮:
                return new MagicN1977Filter();
            case 慵懒的海豹:
                return new MagicNashvilleFilter();
            case 北冰洋的春天:
                return new MagicCoolFilter();
            case 柔和夏日:
                return new MagicWarmFilter();
            default:
                return null;
        }
    }

    public MagicFilterType getCurrentFilterType() {
        return filterType;
    }

    private static class MagicWarmFilter extends GPUImageFilter {
    }
}

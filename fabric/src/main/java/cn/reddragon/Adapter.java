package cn.reddragon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.function.Function;

public class Adapter {
    public static <T> Codec<T> validate(Codec<T> codec, Function<T, DataResult<T>> function) {
        if (codec instanceof MapCodec.MapCodecCodec<T> mapCodecCodec) {
            return validate(mapCodecCodec.codec(), function).codec();
        } else {
            return codec.flatXmap(function, function);
        }
    }
    public static <T> MapCodec<T> validate(MapCodec<T> mapCodec, Function<T, DataResult<T>> function) {
        return mapCodec.flatXmap(function, function);
    }
}

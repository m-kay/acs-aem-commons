package com.adobe.acs.commons.images.transformers.impl;

import com.adobe.acs.commons.images.ImageTransformer;
import com.day.image.Layer;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component(
        label = "Image Transformer - Max width or height resizing",
        description =
                "ImageTransformer that resizes the layer. Accepts two Integer params: height and width. " +
                "Only width or height will be scaled and the other dimension will be calculated to keep original ratio. " +
                "If the original image is smaller than the configured dimensions the image won't be resized")
@Properties({
        @Property(
                name = ImageTransformer.PROP_TYPE,
                value = MaxWidthOrHeightTransformerImpl.TYPE,
                propertyPrivate = true
        )
})
@Service(value = ImageTransformer.class)
public class MaxWidthOrHeightTransformerImpl implements ImageTransformer {
    private static final Logger log = LoggerFactory.getLogger(MaxWidthOrHeightTransformerImpl.class);

    @Property(label = "Allow upscaling images", boolValue = false)
    static final String ALLOW_UPSCALING_PROPERTY = "maxwidthorheight.allowUpscaling";
    private boolean allowUpscaling;

    static final String TYPE = "maxwidthorheight";

    private static final String KEY_WIDTH = "width";

    private static final String KEY_WIDTH_ALIAS = "w";

    private static final String KEY_HEIGHT = "height";

    private static final String KEY_HEIGHT_ALIAS = "h";


    @Override
    public final Layer transform(final Layer layer, final ValueMap properties) {
        if (properties == null || properties.isEmpty()) {
            log.warn("Transform [ {} ] requires parameters.", TYPE);
            return layer;
        }

        log.debug("Transforming with [ {} ]", TYPE);

        int originalWidth = layer.getWidth();
        int originalHeight = layer.getHeight();
        int width = properties.get(KEY_WIDTH, properties.get(KEY_WIDTH_ALIAS, originalWidth));
        int height = properties.get(KEY_HEIGHT, properties.get(KEY_HEIGHT_ALIAS, originalHeight));


        if((float) width / originalWidth < (float) height / originalHeight){
            final float aspect = (float) width / layer.getWidth();
            height = Math.round(layer.getHeight() * aspect);
        }
        else {
            final float aspect = (float) height / layer.getHeight();
            width = Math.round(layer.getWidth() * aspect);
        }

        //only resize if image is not upscaled or upscaling is allowed explicitly
        if(allowUpscaling || (!(width > originalWidth) && !(height > originalHeight))){
            layer.resize(width, height);
        }

        return layer;
    }

    @Activate
    protected final void activate(final Map<String, String> config) {
        allowUpscaling = PropertiesUtil.toBoolean(config.get(ALLOW_UPSCALING_PROPERTY), false);
    }
}
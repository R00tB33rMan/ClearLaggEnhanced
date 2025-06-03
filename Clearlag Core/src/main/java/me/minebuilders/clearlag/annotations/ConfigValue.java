package me.minebuilders.clearlag.annotations;

import me.minebuilders.clearlag.config.ConfigKey;
import me.minebuilders.clearlag.config.ConfigValueType;

import java.lang.annotation.*;

/**
 * Created by TCP on 2/3/2016.
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

public @interface ConfigValue {

    /**
     * The configuration path as a string (legacy method)
     * @return The configuration path
     */
    String path() default "";

    /**
     * The configuration path as a ConfigKey enum
     * @return The configuration key
     */
    ConfigKey key() default ConfigKey.NONE;

    /**
     * Legacy path for backward compatibility
     * @return The legacy path
     */
    String legacyPath() default "";

    /**
     * The type of configuration value
     * @return The configuration value type
     */
    ConfigValueType valueType() default ConfigValueType.PRIMITIVE;

}

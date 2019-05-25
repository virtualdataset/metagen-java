/*
 *   Copyright 2017 jshook
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.virtdata;

import io.virtdata.core.Bindings;
import io.virtdata.core.VirtData;
import io.virtdata.util.ModuleInfo;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class BindingsIntegratedTestIT {

    @BeforeClass
    public static void showModuleInfo() {
        ModuleInfo.logModuleNamesDebug(BindingsIntegratedTestIT.class);
    }

    @Test
    public void testGetNamedIteratedSuffixMap() {
        Bindings bindings = VirtData.getTemplate(
                "mod5", "Mod(5)",
                "mod7", "Mod(7)"
        ).resolveBindings();

        Map<String, Object> map = bindings.getIteratedSuffixMap(11, 2, "mod5");
        assertThat(map).hasSize(2);
        assertThat(map.get("mod50")).isEqualTo(1L);
        assertThat(map.get("mod51")).isEqualTo(2L);
    }

    @Test
    public void testIteratedSuffixMap() {
        Bindings bindings = VirtData.getTemplate(
                "mod5", "Mod(5)",
                "mod7", "Mod(7)"
        ).resolveBindings();

        Map<String, Object> map = bindings.getIteratedSuffixMap(11, 2);
        assertThat(map).hasSize(4);
        assertThat(map.get("mod50")).isEqualTo(1L);
        assertThat(map.get("mod70")).isEqualTo(4L);
        assertThat(map.get("mod51")).isEqualTo(2L);
        assertThat(map.get("mod71")).isEqualTo(5L);
    }


    public void testIteratedMaps() {
        Bindings bindings = VirtData.getTemplate(
                "mod5", "Mod(5)",
                "mod7", "Mod(7)"
        ).resolveBindings();

        List<Map<String, Object>> maps = bindings.getIteratedMaps(11, 2);
        assertThat(maps).hasSize(2);
        assertThat(maps.get(0)).hasSize(2);
        assertThat(maps.get(1)).hasSize(2);
        assertThat(maps.get(0).get("mod5")).isEqualTo(1L);
        assertThat(maps.get(0).get("mod7")).isEqualTo(4L);
        assertThat(maps.get(1).get("mod5")).isEqualTo(2L);
        assertThat(maps.get(1).get("mod7")).isEqualTo(5L);

    }

    @Test
    public void testMapResult() {
        Bindings bindings = VirtData.getTemplate(
                "mod5", "Mod(5)",
                "mod7", "Mod(7)"
        ).resolveBindings();

        Map<String, Object> map = bindings.getLazyMap(12);
        assertThat(map.get("mod5")).isEqualTo(2L);
        assertThat(map.get("mod7")).isEqualTo(5L);
    }

    @Test
    public void testMapGetter() {
        Bindings bindings = VirtData.getTemplate(
                "mod5", "Mod(5)",
                "mod7", "Mod(7)"
        ).resolveBindings();
        Map<String, Object> map = bindings.getAllMap(13);
        assertThat(map).hasSize(2);
        assertThat(map.get("mod5")).isEqualTo(3L);
        assertThat(map.get("mod7")).isEqualTo(6L);
    }

    @Test
    public void testMapSetter() {
        Bindings bindings = VirtData.getTemplate(
            "mod5", "Mod(5)",
            "mod7", "Mod(7)"
                ).resolveBindings();

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        bindings.setMap(map, 12);
        assertThat(map.size()).isEqualTo(2);
        assertThat(map).containsEntry("mod5", 2L);
        assertThat(map).containsEntry("mod7", 5L);
    }

    @Test
    public void testMapUpdater() {
        Bindings bindings = VirtData.getTemplate(
            "mod5", "Mod(5)",
            "mod7", "Mod(7)",
            "mod13", "Mod(13)"
            ).resolveBindings();

        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>() {{
            put("mod5", null);
            put("mod13", "not-it");
        }};
        bindings.updateMap(map, 12);
        assertThat(map.size()).isEqualTo(2);
        assertThat(map).containsEntry("mod5", 2L);
        assertThat(map).containsEntry("mod13", 12L);
    }

    @Test
    public void testSetNamedFieldsIterated() {
        Bindings bindings = VirtData.getTemplate(
            "mod5", "Mod(5)",
            "mod7", "Mod(7)"
        ).resolveBindings();

        final StringBuilder sb = new StringBuilder();
        Bindings.FieldSetter fs = new Bindings.FieldSetter() {
            @Override
            public void setField(String name, Object value) {
                sb.append(name).append("=").append(value).append(";");
            }
        };

        bindings.setNamedFieldsIterated(fs, 12, 2, "mod5");
        assertThat(sb.toString()).isEqualTo("mod50=2;mod51=3;");
        bindings.setNamedFieldsIterated(fs, 12, 2, "mod7");
        assertThat(sb.toString()).isEqualTo("mod50=2;mod51=3;mod70=5;mod71=6;");
    }

    @Test
    public void testSetFieldsIterated() {
        Bindings bindings = VirtData.getTemplate(
            "mod5", "Mod(5)",
            "mod7", "Mod(7)"
        ).resolveBindings();

        final StringBuilder sb = new StringBuilder();
        Bindings.FieldSetter fs = new Bindings.FieldSetter() {
            @Override
            public void setField(String name, Object value) {
                sb.append(name).append("=").append(value).append(";");
            }
        };

        bindings.setAllFieldsIterated(fs, 12, 2);
        assertThat(sb.toString()).isEqualTo("mod50=2;mod50=2;mod71=6;mod71=6;");
    }

    @Test
    public void testSetAllFields() {
        Bindings bindings = VirtData.getTemplate(
            "mod5", "Mod(5)",
            "mod7", "Mod(7)"
        ).resolveBindings();

        final StringBuilder sb = new StringBuilder();
        Bindings.FieldSetter fs = new Bindings.FieldSetter() {
            @Override
            public void setField(String name, Object value) {
                sb.append(name).append("=").append(value).append(";");
            }
        };

        bindings.setAllFields(fs, 12);
        assertThat(sb.toString()).isEqualTo("mod5=2;mod7=5;");
    }

    @Test
    public void testSetNamedFields() {
        Bindings bindings = VirtData.getTemplate(
            "mod5", "Mod(5)",
            "mod7", "Mod(7)",
            "mod13", "Mod(13)"
        ).resolveBindings();

        final StringBuilder sb = new StringBuilder();
        Bindings.FieldSetter fs = new Bindings.FieldSetter() {
            @Override
            public void setField(String name, Object value) {
                sb.append(name).append("=").append(value).append(";");
            }
        };

        bindings.setNamedFields(fs, 12, "mod5", "mod7");
        assertThat(sb.toString()).isEqualTo("mod5=2;mod7=5;");
    }


    @Test
    public void testDirectFunctionalInterfaceLongUnary() {
        LongUnaryOperator f = VirtData.getFunction("Add(5L)", LongUnaryOperator.class);
        assertThat(f).isNotNull();
        assertThat(f.getClass()==LongUnaryOperator.class);
    }

    // This test is for the added boolean type, as well as ToString(func) types, as well as the
    // ability to have the MapTemplate optionally promote types to string values or leave them as
    // their native object values
    @Test
    public void testMapElementTypes() {
        LongFunction f = VirtData.getFunction(
                "MapTemplate(false, HashRange(2,10),ToString(HashRange(1000,9999)),ToLongFunction(HashRange(0,99)));",
                LongFunction.class
        );
        Object o1 = f.apply(1L);
        assertThat(o1).isInstanceOf(Map.class);
        Map map = Map.class.cast(o1);
        //<{"3294"=87, "4506"=19, "5521"=56, "7626"=46}>
        assertThat(map).containsOnlyKeys("3294","4506","5521","7626");
        assertThat(map).containsValues(87,19,56,46);

    }
}
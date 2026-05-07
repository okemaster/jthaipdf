package com.googlecode.jthaipdf.util;

import static com.googlecode.jthaipdf.util.ThaiDisplayUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ThaiDisplayUtils")
class ThaiDisplayUtilsTest {

    private static String s(char... chars) {
        return new String(chars);
    }

    private static Arguments rule(String name, String input, String expected) {
        return Arguments.of(name, input, expected);
    }

    @Nested
    @DisplayName("pass-through (no offending Thai combinations)")
    class PassThrough {

        @ParameterizedTest(name = "[{index}] \"{0}\" stays unchanged")
        @ValueSource(strings = {
                "",
                "Hello, world!",
                "ABC 123",
                "ก",
                "กา",
                "ขนม",
                "สวัสดี",       // ั over ว, ี over ด — neither base is up-tail
                "ภาษาไทย"
        })
        void leaves_input_unchanged(String input) {
            assertEquals(input, toDisplayString(input));
        }

        @Test
        @DisplayName("empty char[] returns empty char[]")
        void empty_char_array() {
            assertArrayEquals(new char[0], toDisplayString(new char[0]));
        }
    }

    @Nested
    @DisplayName("up-tail base (ป ฝ ฟ ฬ) + upper level 1 mark → shift left")
    class UpTailWithUpperLevel1 {

        static Stream<Arguments> cases() {
            return Stream.of(
                    rule("ป + MAI_HAN_AKAT",  s(PO_PLA, MAI_HAN_AKAT),  s(PO_PLA, MAI_HAN_AKAT_LEFT_SHIFT)),
                    rule("ฝ + SARA_I",        s(FO_FA, SARA_I),         s(FO_FA, SARA_I_LEFT_SHIFT)),
                    rule("ฟ + SARA_Ii",       s(FO_FAN, SARA_Ii),       s(FO_FAN, SARA_Ii_LEFT_SHIFT)),
                    rule("ฬ + SARA_Ue",       s(LO_CHULA, SARA_Ue),     s(LO_CHULA, SARA_Ue_LEFT_SHIFT)),
                    rule("ป + SARA_Uee",      s(PO_PLA, SARA_Uee),      s(PO_PLA, SARA_Uee_LEFT_SHIFT)),
                    rule("ป + MAI_TAI_KHU",   s(PO_PLA, MAI_TAI_KHU),   s(PO_PLA, MAI_TAI_KHU_LEFT_SHIFT)),
                    // NIKHAHIT only appears standalone after SARA_AM explosion, but the rule is the same.
                    rule("ป + NIKHAHIT",      s(PO_PLA, NIKHAHIT),      s(PO_PLA, NIKHAHIT_LEFT_SHIFT))
            );
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("cases")
        void shifts_left(String name, String input, String expected) {
            assertEquals(expected, toDisplayString(input));
        }
    }

    @Nested
    @DisplayName("upper level 2 mark over a regular base → pull down")
    class UpperLevel2OverRegularBase {

        static Stream<Arguments> cases() {
            return Stream.of(
                    rule("ก + MAI_EK",       s('ก', MAI_EK),       s('ก', MAI_EK_DOWN)),
                    rule("ก + MAI_THO",      s('ก', MAI_THO),      s('ก', MAI_THO_DOWN)),
                    rule("ก + MAI_TRI",      s('ก', MAI_TRI),      s('ก', MAI_TRI_DOWN)),
                    rule("ก + MAI_CHATTAWA", s('ก', MAI_CHATTAWA), s('ก', MAI_CHATTAWA_DOWN)),
                    rule("ก + THANTHAKHAT",  s('ก', THANTHAKHAT),  s('ก', THANTHAKHAT_DOWN))
            );
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("cases")
        void pulls_down(String name, String input, String expected) {
            assertEquals(expected, toDisplayString(input));
        }
    }

    @Nested
    @DisplayName("upper level 2 mark over an up-tail base → pull down + shift left")
    class UpperLevel2OverUpTail {

        static Stream<Arguments> cases() {
            return Stream.of(
                    rule("ป + MAI_EK",       s(PO_PLA, MAI_EK),       s(PO_PLA, MAI_EK_PULL_DOWN_AND_LEFT_SHIFT)),
                    rule("ป + MAI_THO",      s(PO_PLA, MAI_THO),      s(PO_PLA, MAI_THO_PULL_DOWN_AND_LEFT_SHIFT)),
                    rule("ป + MAI_TRI",      s(PO_PLA, MAI_TRI),      s(PO_PLA, MAI_TRI_PULL_DOWN_AND_LEFT_SHIFT)),
                    rule("ป + MAI_CHATTAWA", s(PO_PLA, MAI_CHATTAWA), s(PO_PLA, MAI_CHATTAWA_PULL_DOWN_AND_LEFT_SHIFT)),
                    rule("ป + THANTHAKHAT",  s(PO_PLA, THANTHAKHAT),  s(PO_PLA, THANTHAKHAT_PULL_DOWN_AND_LEFT_SHIFT))
            );
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("cases")
        void pulls_down_and_shifts_left(String name, String input, String expected) {
            assertEquals(expected, toDisplayString(input));
        }
    }

    @Nested
    @DisplayName("upper level 2 stacked on a left-shifted upper level 1 mark → shift left only")
    class UpperLevel2OverShiftedUpperLevel1 {

        // ป + ิ + ่  →  ป + SARA_I_LEFT_SHIFT + MAI_EK_LEFT_SHIFT
        @Test
        void shifts_left_when_preceding_mark_was_already_shifted() {
            String input = s(PO_PLA, SARA_I, MAI_EK);
            String expected = s(PO_PLA, SARA_I_LEFT_SHIFT, MAI_EK_LEFT_SHIFT);
            assertEquals(expected, toDisplayString(input));
        }

        // ก + ิ + ่ — pch is plain (un-shifted) upper level 1, so MAI_EK is left alone
        @Test
        void leaves_level2_alone_when_preceding_upper1_was_not_shifted() {
            String input = s('ก', SARA_I, MAI_EK);
            assertEquals(input, toDisplayString(input));
        }
    }

    @Nested
    @DisplayName("upper level 2 above a lower-level vowel re-evaluates against the base two back")
    class UpperLevel2WithLowerLevelLookback {

        // ปุ้ → upper-level-2 above SARA_U above ป (up-tail) → pull-down-and-shift-left
        @Test
        void up_tail_base_two_back_triggers_pull_down_and_shift_left() {
            String input = s(PO_PLA, SARA_U, MAI_THO);
            String expected = s(PO_PLA, SARA_U, MAI_THO_PULL_DOWN_AND_LEFT_SHIFT);
            assertEquals(expected, toDisplayString(input));
        }

        // กุ้ → upper-level-2 above SARA_U above ก (regular) → pull down
        @Test
        void regular_base_two_back_triggers_pull_down() {
            String input = s('ก', SARA_U, MAI_THO);
            String expected = s('ก', SARA_U, MAI_THO_DOWN);
            assertEquals(expected, toDisplayString(input));
        }
    }

    @Nested
    @DisplayName("down-tail base + lower-level vowel")
    class DownTailWithLowerLevel {

        static Stream<Arguments> cutTailCases() {
            return Stream.of(
                    rule("ฐ + SARA_U",   s(THO_THAN, SARA_U),  s(THO_THAN_CUT_TAIL, SARA_U)),
                    rule("ฐ + SARA_UU",  s(THO_THAN, SARA_UU), s(THO_THAN_CUT_TAIL, SARA_UU)),
                    rule("ฐ + PHINTHU",  s(THO_THAN, PHINTHU), s(THO_THAN_CUT_TAIL, PHINTHU)),
                    rule("ญ + SARA_U",   s(YO_YING, SARA_U),   s(YO_YING_CUT_TAIL, SARA_U)),
                    rule("ญ + SARA_UU",  s(YO_YING, SARA_UU),  s(YO_YING_CUT_TAIL, SARA_UU))
            );
        }

        // ฐ and ญ have a dedicated cut-tail glyph, so the base is rewritten and the vowel left alone.
        @ParameterizedTest(name = "[{index}] {0} cuts the tail off the base")
        @MethodSource("cutTailCases")
        void cuts_tail_when_base_has_dedicated_glyph(String name, String input, String expected) {
            assertEquals(expected, toDisplayString(input));
        }

        static Stream<Arguments> pullDownCases() {
            return Stream.of(
                    rule("ฎ + SARA_U",   s(DO_CHADA, SARA_U),  s(DO_CHADA, SARA_U_DOWN)),
                    rule("ฏ + SARA_UU",  s(TO_PATAK, SARA_UU), s(TO_PATAK, SARA_UU_DOWN)),
                    rule("ฤ + PHINTHU",  s(RU, PHINTHU),       s(RU, PHINTHU_DOWN)),
                    rule("ฦ + SARA_U",   s(LU, SARA_U),        s(LU, SARA_U_DOWN))
            );
        }

        // The remaining down-tail consonants have no cut-tail variant, so we pull the vowel down instead.
        @ParameterizedTest(name = "[{index}] {0} pulls the vowel down")
        @MethodSource("pullDownCases")
        void pulls_down_when_base_has_no_cut_tail_glyph(String name, String input, String expected) {
            assertEquals(expected, toDisplayString(input));
        }
    }

    @Nested
    @DisplayName("SARA AM (ำ) explosion")
    class SaraAmExplosion {

        // กำ → ก + NIKHAHIT + SARA_AA — base is regular, NIKHAHIT lands after the base.
        @Test
        void splits_around_regular_base() {
            String input = s('ก', SARA_AM);
            String expected = s('ก', NIKHAHIT, SARA_AA);
            assertEquals(expected, toDisplayString(input));
        }

        // ปำ → ป + NIKHAHIT_LEFT_SHIFT + SARA_AA — NIKHAHIT shifted because base is up-tail.
        @Test
        void shifts_nikhahit_left_over_up_tail_base() {
            String input = s(PO_PLA, SARA_AM);
            String expected = s(PO_PLA, NIKHAHIT_LEFT_SHIFT, SARA_AA);
            assertEquals(expected, toDisplayString(input));
        }

        // ก่ำ → upper-level-2 sits between base and SARA_AM; NIKHAHIT is inserted before the level-2 mark
        // so it can stack with the base, and the level-2 mark sits on top untouched.
        @Test
        void inserts_nikhahit_before_upper_level_2_mark() {
            String input = s('ก', MAI_EK, SARA_AM);
            String expected = s('ก', NIKHAHIT, MAI_EK, SARA_AA);
            assertEquals(expected, toDisplayString(input));
        }

        // ป่ำ → up-tail + upper-level-2 + SARA_AM. NIKHAHIT is inserted, gets shifted left over ป,
        // then MAI_EK stacks on the now-shifted NIKHAHIT, so it shifts left too.
        @Test
        void chains_through_up_tail_and_upper_level_2() {
            String input = s(PO_PLA, MAI_EK, SARA_AM);
            String expected = s(PO_PLA, NIKHAHIT_LEFT_SHIFT, MAI_EK_LEFT_SHIFT, SARA_AA);
            assertEquals(expected, toDisplayString(input));
        }

        @Test
        void handles_multiple_sara_am_in_one_string() {
            String input = s('ก', SARA_AM, 'ข', SARA_AM);
            String expected = s('ก', NIKHAHIT, SARA_AA, 'ข', NIKHAHIT, SARA_AA);
            assertEquals(expected, toDisplayString(input));
        }
    }

    @Nested
    @DisplayName("real-word smoke tests")
    class RealWords {

        // ปิ่น (a hairpin)
        @Test
        void pin() {
            String input = s(PO_PLA, SARA_I, MAI_EK, 'น');
            String expected = s(PO_PLA, SARA_I_LEFT_SHIFT, MAI_EK_LEFT_SHIFT, 'น');
            assertEquals(expected, toDisplayString(input));
        }

        // ปุ่ม (button)
        @Test
        void pum() {
            String input = s(PO_PLA, SARA_U, MAI_EK, 'ม');
            String expected = s(PO_PLA, SARA_U, MAI_EK_PULL_DOWN_AND_LEFT_SHIFT, 'ม');
            assertEquals(expected, toDisplayString(input));
        }

        // ใหญ่ (big) — ญ here has no following lower vowel, so it stays whole; MAI_EK over ญ pulls down.
        @Test
        void yai() {
            String input = s('ใ', 'ห', YO_YING, MAI_EK);
            String expected = s('ใ', 'ห', YO_YING, MAI_EK_DOWN);
            assertEquals(expected, toDisplayString(input));
        }
    }

    @Nested
    @DisplayName("overloads agree with the String form")
    class Overloads {

        private static final String INPUT = "ปิ่น" + " " + "ปุ่ม";
        private static final String EXPECTED = new String(new char[]{
                PO_PLA, SARA_I_LEFT_SHIFT, MAI_EK_LEFT_SHIFT, 'น',
                ' ',
                PO_PLA, SARA_U, MAI_EK_PULL_DOWN_AND_LEFT_SHIFT, 'ม'
        });

        @Test
        void char_array_overload() {
            assertArrayEquals(EXPECTED.toCharArray(), toDisplayString(INPUT.toCharArray()));
        }

        @Test
        void char_array_overload_does_not_mutate_input() {
            char[] original = INPUT.toCharArray();
            char[] snapshot = original.clone();
            char[] result = toDisplayString(original);
            assertArrayEquals(snapshot, original, "input array must not be mutated");
            assertArrayEquals(EXPECTED.toCharArray(), result);
        }

        @Test
        void string_buffer_overload_mutates_in_place() {
            StringBuffer buf = new StringBuffer(INPUT);
            toDisplayString(buf);
            assertEquals(EXPECTED, buf.toString());
        }

        @Test
        void string_buffer_overload_handles_sara_am_length_change() {
            StringBuffer buf = new StringBuffer(s('ก', SARA_AM));
            toDisplayString(buf);
            assertEquals(s('ก', NIKHAHIT, SARA_AA), buf.toString());
            assertEquals(3, buf.length());
        }
    }

    @Nested
    @DisplayName("public char-level helpers")
    class CharHelpers {

        @Test
        void isLowerLevel_is_true_only_for_lower_vowels() {
            assertAll(
                    () -> assertTrue(isLowerLevel(SARA_U)),
                    () -> assertTrue(isLowerLevel(SARA_UU)),
                    () -> assertTrue(isLowerLevel(PHINTHU)),
                    () -> assertFalse(isLowerLevel(MAI_EK)),
                    () -> assertFalse(isLowerLevel(SARA_I)),
                    () -> assertFalse(isLowerLevel('ก'))
            );
        }

        @Test
        void shiftLeft_maps_each_known_mark_and_passes_others_through() {
            assertAll(
                    () -> assertEquals(MAI_EK_LEFT_SHIFT,        shiftLeft(MAI_EK)),
                    () -> assertEquals(MAI_THO_LEFT_SHIFT,       shiftLeft(MAI_THO)),
                    () -> assertEquals(MAI_TRI_LEFT_SHIFT,       shiftLeft(MAI_TRI)),
                    () -> assertEquals(MAI_CHATTAWA_LEFT_SHIFT,  shiftLeft(MAI_CHATTAWA)),
                    () -> assertEquals(MAI_HAN_AKAT_LEFT_SHIFT,  shiftLeft(MAI_HAN_AKAT)),
                    () -> assertEquals(SARA_I_LEFT_SHIFT,        shiftLeft(SARA_I)),
                    () -> assertEquals(SARA_Ii_LEFT_SHIFT,       shiftLeft(SARA_Ii)),
                    () -> assertEquals(SARA_Ue_LEFT_SHIFT,       shiftLeft(SARA_Ue)),
                    () -> assertEquals(SARA_Uee_LEFT_SHIFT,      shiftLeft(SARA_Uee)),
                    () -> assertEquals(MAI_TAI_KHU_LEFT_SHIFT,   shiftLeft(MAI_TAI_KHU)),
                    () -> assertEquals(NIKHAHIT_LEFT_SHIFT,      shiftLeft(NIKHAHIT)),
                    () -> assertEquals('ก',                       shiftLeft('ก')),
                    () -> assertEquals(SARA_U,                    shiftLeft(SARA_U))
            );
        }

        @Test
        void pullDownAndShiftLeft_maps_each_known_mark_and_passes_others_through() {
            assertAll(
                    () -> assertEquals(MAI_EK_PULL_DOWN_AND_LEFT_SHIFT,       pullDownAndShiftLeft(MAI_EK)),
                    () -> assertEquals(MAI_THO_PULL_DOWN_AND_LEFT_SHIFT,      pullDownAndShiftLeft(MAI_THO)),
                    () -> assertEquals(MAI_TRI_PULL_DOWN_AND_LEFT_SHIFT,      pullDownAndShiftLeft(MAI_TRI)),
                    () -> assertEquals(MAI_CHATTAWA_PULL_DOWN_AND_LEFT_SHIFT, pullDownAndShiftLeft(MAI_CHATTAWA)),
                    () -> assertEquals(MAI_HAN_AKAT_LEFT_SHIFT,               pullDownAndShiftLeft(MAI_HAN_AKAT)),
                    () -> assertEquals(THANTHAKHAT_PULL_DOWN_AND_LEFT_SHIFT,  pullDownAndShiftLeft(THANTHAKHAT)),
                    () -> assertEquals('ก',                                    pullDownAndShiftLeft('ก'))
            );
        }
    }
}

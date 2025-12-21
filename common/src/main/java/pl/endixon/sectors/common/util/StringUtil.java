/*
 *
 *  EndSectors  Non-Commercial License
 *  (c) 2025 Endixon
 *
 *  Permission is granted to use, copy, and
 *  modify this software **only** for personal
 *  or educational purposes.
 *
 *   Commercial use, redistribution, claiming
 *  this work as your own, or copying code
 *  without explicit permission is strictly
 *  prohibited.
 *
 *  Visit https://github.com/Endixon/EndSectors
 *  for more info.
 *
 */

package pl.endixon.sectors.common.util;

import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {

    public static String join(List<String> input, String separator) {
        if (input == null || input.size() <= 0)
            return "";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < input.size(); i++) {
            sb.append(input.get(i));

            if (i != input.size() - 1)
                sb.append(separator);
        }

        return sb.toString();
    }
}

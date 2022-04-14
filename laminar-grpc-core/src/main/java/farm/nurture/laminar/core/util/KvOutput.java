/*
 *  Copyright 2022 Nurture.Farm
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package farm.nurture.laminar.core.util;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KvOutput {

    private String code = null;
    private String desc = null;
    private boolean status = true;
    public KvOutput(boolean status, String code, String desc) {
        this.setStatus(status);
        this.setCode(code);
        this.setDesc(desc);
    }

    public String getJSon() {
        StringBuilder errors = new StringBuilder();
        errors.append("{");
        errors.append("\"code\":\"").append(getCode()).append("\",");
        errors.append("\"description\":\"").append(getDesc()).append("\"");
        errors.append("}");
        return errors.toString();
    }
}

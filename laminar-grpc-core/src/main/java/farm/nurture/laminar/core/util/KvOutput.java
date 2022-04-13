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

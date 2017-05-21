package ca.projecthermes.projecthermes.mock.networking.payload;

import ca.projecthermes.projecthermes.networking.payload.IPayload;

public class TestPayload implements IPayload {
    public int val;
    public TestPayload(int val) {
        this.val = val;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof TestPayload) && (this.val == ((TestPayload)other).val);
    }
}

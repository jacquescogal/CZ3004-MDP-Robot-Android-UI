package com.example.mdpremotecontrol.enums;

public enum GridState {
        EMTPY(0),
        BLOCK(1),
        IMAGEBLOCK(2),
        ROBOT(3);
        private final int value;

        private GridState(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
}

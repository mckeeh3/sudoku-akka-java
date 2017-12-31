package sudoku;

import java.io.Serializable;

class SetCell implements Serializable {
    final int row;
    final int col;
    final int value;
    final String who;

    SetCell(int row, int col, int value, String who) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.who = who;
    }

    @Override
    public String toString() {
        return String.format("%s[%d, %d, %d, '%s']", getClass().getSimpleName(), row, col, value, who);
    }
}

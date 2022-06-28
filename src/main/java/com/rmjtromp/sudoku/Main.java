package com.rmjtromp.sudoku;

import java.awt.EventQueue;

public final class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(Sudoku::createNewInstance);
	}

}

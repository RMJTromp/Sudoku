package com.rmjtromp.sudoku;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public final class Sudoku extends JFrame {

	private static final Random random = new Random();
	private static Sudoku sudoku = null;

	private Cell selectedCell = null;
	private static Font robotoFont = null;
	private final List<Cell> sudokuCells = new ArrayList<>();
	private final List<Block> sudokuBlocks = new ArrayList<>();
	private final List<Row> sudokuRows = new ArrayList<>();
	private final List<Column> sudokuColums = new ArrayList<>();
	
	public static Sudoku createNewInstance() {
		try{
			robotoFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Sudoku.class.getResourceAsStream("/Roboto-Regular.ttf")));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(robotoFont);
        } catch(Exception e){
        	e.printStackTrace();
        }
		return new Sudoku();
	}

	private Sudoku() {
		sudoku = this;
        setSize(1016, 640);
        setTitle("Sudoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(Color.WHITE);
        
        for(int x = 1; x < 10; x++) {
        	for(int y = 1; y < 10; y++) {
        		Cell cell = new Cell(x, y);
        		sudokuCells.add(cell);
        	}
        }
        
        for(int i = 1; i < 10; i++) {
        	final int b = i;
        	sudokuColums.add(new Column(i, sudokuCells.stream().filter(cell -> cell.x == b).toList().toArray(new Cell[0])));
            sudokuRows.add(new Row(i, sudokuCells.stream().filter(cell -> cell.y == b).toList().toArray(new Cell[0])));
        }
        
        int blockId = 1;
        List<int[]> a = new ArrayList<>();
        a.add(new int[] {1, 3});
        a.add(new int[] {4, 6});
        a.add(new int[] {7, 9});
        
        for(int x = 1; x < 4; x++) {
        	for(int y = 1; y < 4; y++) {
        		int[] b = a.get(x-1); int[] c = a.get(y-1);
        		Cell[] cells = sudokuCells.stream().filter(cell -> cell.x >= b[0] && cell.x <= b[1] && cell.y >= c[0] && cell.y <= c[1]).toList().toArray(new Cell[0]);
                sudokuBlocks.add(new Block(blockId, cells));
                blockId++;
        	}
        }
        
        generate();

        JPanel cells = new JPanel() {

			@Override
        	public void paint(Graphics g) {
        		super.paint(g);
        		Graphics2D g2 = (Graphics2D) g;
        		float spacing = getWidth() / 9.0F;
        		
        		// draw background color if hovering
        		Point point = getMousePosition();
        		if(point != null) {
        			int[] cursorCoordinate = new int[2];

        			for(int i = 0; i < 9; i++) {
        				float x = i * spacing;
        				if(point.getX() >= x && point.getX() < x + spacing) cursorCoordinate[0] = i;
        			}
        			
        			for(int i = 0; i < 9; i++) {
        				float y = i * spacing;
        				if(point.getY() >= y && point.getY() < y + spacing) cursorCoordinate[1] = i;
        			}


            		g2.setColor(new Color(226, 231, 237, 76));
        			g2.fillRect((int) (cursorCoordinate[0] * spacing), 0, (int) spacing, getHeight());
        			g2.fillRect(0, (int) (cursorCoordinate[1] * spacing), getWidth(), (int) spacing);
        		}
        		
        		if(selectedCell != null) {
            		g2.setColor(new Color(226, 231, 237, 150));
        			g2.fillRect((int) ((selectedCell.x - 1) * spacing), 0, (int) spacing, getHeight());
        			g2.fillRect(0, (int) ((selectedCell.y - 1) * spacing), getWidth(), (int) spacing);
        		}
        		
        		// draw thin lines
        		g2.setColor(new Color(208, 213, 224));
                g2.setStroke(new BasicStroke(1));
                
        		for(int i = 1; i < 9; i++) {
        			if(i % 3 != 0) {
        				Line2D line = new Line2D.Float(spacing * i, 0, spacing * i, getHeight());
                        g2.draw(line);
        			}
        		}
        		
        		for(int i = 1; i < 9; i++) {
        			if(i % 3 != 0) {
                        Line2D line = new Line2D.Float(0, spacing * i, getWidth(), spacing * i);
                        g2.draw(line);
        			}
        		}
        		
        		// draw black lines
        		g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
        		for(int i = 1; i < 9; i++) {
        			if(i % 3 == 0) {
        				Line2D line = new Line2D.Float(spacing * i, 0, spacing * i, getHeight());
                        g2.draw(line);
        			}
        		}
        		
        		for(int i = 1; i < 9; i++) {
        			if(i % 3 == 0) {
                        Line2D line = new Line2D.Float(0, spacing * i, getWidth(), spacing * i);
                        g2.draw(line);
        			}
        		}

        		Font font = robotoFont != null ? robotoFont.deriveFont(35F) : g.getFont().deriveFont(35F);
        		Font pencil = robotoFont != null ? robotoFont.deriveFont(14F) : g.getFont().deriveFont(14F);
        		for(int x = 0; x < 9; x++) {
        			for(int y = 0; y < 9; y++) {
        				Cell cell = getCell(x+1, y+1);
        				if(cell != null) {
        					if(cell.backgroundColor != null) {
        						g2.setColor(cell.backgroundColor);
            					g2.fillRect((int)(x*spacing), (int)(y*spacing), (int)spacing, (int)spacing);
        					}
        					
        					if(cell.isValid()) {
            					if(selectedCell != null && selectedCell.value != 0 && cell.value == selectedCell.value && !cell.color.equals(Cell.errorColor)) {
                					g2.setColor(new Color(51, 93, 178, 50));
                					g2.fillRect((int)(x*spacing), (int)(y*spacing), (int)spacing, (int)spacing);
            					} else if(cell.color.equals(Cell.errorColor)) {
                					g2.setColor(new Color(237, 23, 36, 30));
                					g2.fillRect((int)(x*spacing), (int)(y*spacing), (int)spacing, (int)spacing);
                				}
            					
                				String text = Integer.toString(cell.value);
                				g2.setColor(cell.finalValue ? Cell.defaultColor : cell.color);
                				g2.setFont(font);
                				g2.drawString(text, x*spacing + (spacing / 2 - (int)(g.getFontMetrics().stringWidth(text) / 2D)), y*spacing + spacing / 2 + (int)(g.getFontMetrics().getHeight() / 4D));
        					} else if(!cell.pencils.isEmpty()) {
                				g2.setColor(Color.GRAY);
                				g2.setFont(pencil);
                				float a = spacing / 3F; // length of side of pencil cell
                				
                				float cx = x*spacing; // cell x coordinate
                				float cy = y*spacing; // cell y coordinate
                				float fh = g.getFontMetrics().getHeight() / 4F; // half font height
                				for(int n : cell.pencils) {
                					int px = n % 3; // pencil cell x coordinate
                					if(px == 0) px = 3;
                					int py = ((n-1) / 3); // pencil cell y coordinate
                					
                    				String text = Integer.toString(n);
                    				float fw = g.getFontMetrics().stringWidth(text) / 2f; // half font width
                    				g2.drawString(text, cx + px*a - a + fw, cy + py*a + a - fh);
                				}
        					}
        				}
        			}
        		}
        		paintBorder(g);
        	}
        	
        };
        cells.setPreferredSize(new Dimension(500, 500));
        cells.setBackground(Color.WHITE);
        cells.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
        
        cells.addMouseMotionListener(new MouseMotionAdapter() {
        	
        	@Override
        	public void mouseMoved(MouseEvent e) {
        		super.mouseMoved(e);
        		cells.repaint();
        	}
        	
		});
        
        cells.addMouseListener(new MouseAdapter() {
        	
        	@Override
        	public void mouseEntered(MouseEvent e) {
        		super.mouseEntered(e);
        		cells.repaint();
        	}
        	
        	@Override
        	public void mouseExited(MouseEvent e) {
        		super.mouseExited(e);
        		cells.repaint();
        	}
        	
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		super.mouseClicked(e);
        		if(e.getButton() == MouseEvent.BUTTON1) {
            		selectedCell = null;
            		float spacing = cells.getWidth() / 9.0F;
            		int[] cursorCoordinate = new int[2];

        			for(int i = 0; i < 9; i++) {
        				float x = i * spacing;
        				if(e.getPoint().getX() >= x && e.getPoint().getX() < x + spacing) cursorCoordinate[0] = i;
        			}
        			
        			for(int i = 0; i < 9; i++) {
        				float y = i * spacing;
        				if(e.getPoint().getY() >= y && e.getPoint().getY() < y + spacing) cursorCoordinate[1] = i;
        			}
        			
        			selectedCell = getCell(cursorCoordinate[0]+1, cursorCoordinate[1]+1);
            		cells.repaint();
        		}
        	}
        	
		});
		JPanel container = new JPanel(new GridBagLayout());
		container.add(cells);
        container.setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
		
	        @Override
	        public void mouseClicked(MouseEvent e) {
	        	super.mouseClicked(e);
        		selectedCell = null;
        		cells.repaint();
	        }
        	
        });
        setFocusTraversalKeysEnabled(false);
        addKeyListener(new KeyAdapter() {
        	
        	boolean pencilToggle = false;
        	
        	@Override
        	public void keyTyped(KeyEvent e) {
        		int keyCode = e.getKeyChar();
        		if(keyCode == KeyEvent.VK_ESCAPE) {
        			if(selectedCell != null) {
            			selectedCell = null;
            			cells.repaint();
        			}
        		} else if(keyCode == KeyEvent.VK_TAB) {
        			if(selectedCell == null) selectedCell = getCell(1, 1);
        			else {
        				int x = selectedCell.x;
        				int y = selectedCell.y;
        				if(!e.isShiftDown()) {
        					x++;
            				if(x > 9) {
            					x = 1;
            					y++;
            				}
            				if(y > 9) {
            					x = 1;
            					y = 1;
            				}
        				} else {
        					x--;
            				if(x < 1) {
            					x = 9;
            					y--;
            				}
            				if(y < 1) {
            					x = 9;
            					y = 9;
            				}
        				}
        				selectedCell = getCell(x, y);
        			}
    				cells.repaint();
        		} else if(keyCode == KeyEvent.VK_SPACE) {
        			pencilToggle = !pencilToggle;
        		} else {
        			if(!isSolved() && (Character.isDigit(e.getKeyChar()) || keyCode == KeyEvent.VK_BACK_SPACE) && selectedCell != null) {
        				int val = Character.isDigit(e.getKeyChar()) ? Integer.parseInt(Character.toString(e.getKeyChar())) : 0;
        				if(pencilToggle) {
        					if(val == 0) selectedCell.pencils.clear();
        					else {
        						if(selectedCell.pencils.contains(val)) selectedCell.removePencil(val);
        						else selectedCell.addPencil(val);
        					}
        				} else {
                			selectedCell.setValue(val);
                			selectedCell.color = Cell.userColor;
                			
                			if(val != 0) {
                    			for(Cell cell : selectedCell.getBlock().cells) cell.removePencil(val);
                    			for(Cell cell : selectedCell.getColumn().cells) cell.removePencil(val);
                    			for(Cell cell : selectedCell.getRow().cells) cell.removePencil(val);
                			}
                			
                			sudokuCells.stream().filter(cell -> cell.value != 0).forEach(cell -> {
        						if(cell.asd()) cell.color = Cell.errorColor;
        						else cell.color = cell.finalValue ? Cell.defaultColor : Cell.userColor;
        					});
                			
                			if(isSolved()) {
                				selectedCell = null;

                				List<Cell> stage5 = new ArrayList<>();
                				List<Cell> stage4 = new ArrayList<>();
                				List<Cell> stage3 = new ArrayList<>();
                				List<Cell> stage2 = new ArrayList<>();
                				List<Cell> stage1 = new ArrayList<>();
                				stage1.add(getCell(1, 1));
                				cells.repaint();
                				
                				Timer timer = new Timer();
                				timer.schedule(new TimerTask() {
									@Override
									public void run() {
										selectedCell = null;
										stage5.forEach(cell -> cell.backgroundColor = null);
										stage5.clear();
										stage5.addAll(stage4);
										stage4.clear();
										stage4.addAll(stage3);
										stage3.clear();
										stage3.addAll(stage2);
										stage3.forEach(cell -> cell.setFinal(true));
										stage2.clear();
										stage2.addAll(stage1);
										
										List<Cell> stage0 = new ArrayList<>();
										stage1.forEach(cell -> {
											List<Cell> a = lak(cell);
											a.forEach(o -> {
												if(!stage0.contains(o)) stage0.add(o);
											});
										});
										stage1.clear();
										stage1.addAll(stage0);
										
										if(stage1.isEmpty() && stage5.isEmpty()) timer.cancel();

										stage1.forEach(cell -> cell.backgroundColor = Color.decode("#d0efff"));
										stage2.forEach(cell -> cell.backgroundColor = Color.decode("#2a9df4"));
										stage3.forEach(cell -> cell.backgroundColor = Color.decode("#287bcd"));
										stage4.forEach(cell -> cell.backgroundColor = Color.decode("#2a9df4"));
										stage5.forEach(cell -> cell.backgroundColor = Color.decode("#d0efff"));
										cells.repaint();
									}
								}, 100, 75);
                			}
        				}
            			
            			cells.repaint();
            		}
        		}
        	}
        	
		});
        add(container, BorderLayout.CENTER);
        setVisible(true);
	}
	
	private List<Cell> lak(Cell cell) {
		List<Cell> cells = new ArrayList<>();
		if(cell.x + 1 < 10) cells.add(getCell(cell.x + 1, cell.y));
		if(cell.y + 1 < 10) cells.add(getCell(cell.x, cell.y + 1));
		return cells;
	}
	
	private void generate() {
		boolean solvable = false;
		while(!solvable) {
			generateRandomGrid();
			solvable = isSolved();
		}
		
		// easy 42
		// medium 35
		// hard 28
		// expert 25
		int count = 35;
		
		for(int i = 0; i < count; i++) {
			if(i < 9) sudokuBlocks.get(i).cells[b(8)].setFinal(true);
			else {
				Block block = sudokuBlocks.get(b(8));
				Cell cell = block.cells[b(8)];
				while(cell.finalValue) cell = block.cells[b(8)];
				cell.setFinal(true);
			}
		}
		sudokuCells.stream().filter(cell -> !cell.finalValue).forEach(cell -> cell.setValue(0));
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * https://github.com/mfgravesjr/finished-projects/tree/master/SudokuGridGenerator *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	private int[] generateRandomGrid() {
		ArrayList<Integer> arr = new ArrayList<>(9);
        int[] grid = new int[81];
        for(int i = 1; i <= 9; i++) arr.add(i);
     
        for(int i = 0; i < 81; i++) {
           if(i%9 == 0) Collections.shuffle(arr);
           int perBox = ((i / 3) % 3) * 9 + ((i % 27) / 9) * 3 + (i / 27) * 27 + (i %3);
           grid[perBox] = arr.get(i%9);
        }
        
        boolean[] sorted = new boolean[81];
        
        for(int i = 0; i < 9; i++) {
           boolean backtrack = false;
           for(int a = 0; a<2; a++) {
              boolean[] registered = new boolean[10];
              int rowOrigin = i * 9;
              int colOrigin = i;
           
              ROW_COL: for(int j = 0; j < 9; j++) {
                 int step = (a%2==0? rowOrigin + j: colOrigin + j*9);
                 int num = grid[step];
                 
                 if(!registered[num]) registered[num] = true;
                 else {
                    for(int y = j; y >= 0; y--) {
                       int scan = (a%2==0? i * 9 + y: i + 9 * y);
                       if(grid[scan] == num) {
                          for(int z = (a%2==0? (i%3 + 1) * 3: 0); z < 9; z++) {
                             if(a%2 == 1 && z%3 <= i%3)
                                continue;
                             int boxOrigin = ((scan % 9) / 3) * 3 + (scan / 27) * 27;
                             int boxStep = boxOrigin + (z / 3) * 9 + (z % 3);
                             int boxNum = grid[boxStep];
                             if((!sorted[scan] && !sorted[boxStep] && !registered[boxNum]) || (sorted[scan] && !registered[boxNum] && (a%2==0? boxStep%9==scan%9: boxStep/9==scan/9))) {
                                grid[scan] = boxNum;
                                grid[boxStep] = num;
                                registered[boxNum] = true;
                                continue ROW_COL;
                             } else if(z == 8) {
                                int searchingNo = num;
                                            
                                boolean[] blindSwapIndex = new boolean[81];
                                
                                for(int q = 0; q < 18; q++) {
                                   SWAP: for(int b = 0; b <= j; b++) {
                                      int pacing = (a%2==0? rowOrigin+b: colOrigin+b*9);
                                      if(grid[pacing] == searchingNo) {
                                         int adjacentCell;
                                         int adjacentNo;
                                         int decrement = (a%2==0? 9: 1);
                                         
                                         for(int c = 1; c < 3 - (i % 3); c++) {
                                            adjacentCell = pacing + (a%2==0? (c + 1)*9: c + 1);
                                            
                                            if((a%2==0 && adjacentCell >= 81) || (a%2==1 && adjacentCell % 9 == 0)) adjacentCell -= decrement;
                                            else {
                                               adjacentNo = grid[adjacentCell];
                                               if(i%3!=0 || c!=1 || blindSwapIndex[adjacentCell] || registered[adjacentNo]) adjacentCell -= decrement;
                                            }
                                            adjacentNo = grid[adjacentCell];
                                            
                                            if(!blindSwapIndex[adjacentCell]) {
                                               blindSwapIndex[adjacentCell] = true;
                                               grid[pacing] = adjacentNo;
                                               grid[adjacentCell] = searchingNo;
                                               searchingNo = adjacentNo;
                                                     
                                               if(!registered[adjacentNo]) {
                                                  registered[adjacentNo] = true;
                                                  continue ROW_COL;
                                               }
                                               break SWAP;
                                            }
                                         }
                                      }
                                   }
                                }
                                backtrack = true;
                                break ROW_COL;
                             }
                          }
                       }
                    }
                 }
              }
              
              if(a%2==0)
                 for(int j = 0; j < 9; j++) sorted[i*9+j] = true;
              else if(!backtrack) 
                 for(int j = 0; j < 9; j++) sorted[i+j*9] = true;
              else {
                 backtrack = false;
                 for(int j = 0; j < 9; j++) sorted[i*9+j] = false;
                 for(int j = 0; j < 9; j++) sorted[(i-1)*9+j] = false;
                 for(int j = 0; j < 9; j++) sorted[i-1+j*9] = false;
                 i-=2;
              }
           }
        }
        
		int i = 0;
        for(int x = 1; x < 10; x++) {
        	for(int y = 1; y < 10; y++) {
        		getCell(x, y).setValue(grid[i]);
        		i++;
        	}
        }
        
        return grid;
	}
	
	private int b(int max) {
		return b(0, max);
	}
	
	private int b(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}
	
	private Cell getCell(int x, int y) {
		if(x >= 1 && x <= 9 && y >= 1 && y <= 9) {
			List<Cell> res = sudokuCells.stream().filter(cell -> cell.x == x && cell.y == y).toList();
			return res.get(0);
		}
		throw new IllegalArgumentException("Cell coordinates must be between 1 and 9");
	}

	@SuppressWarnings("unused")
	private Block getBlock(int id) {
		if(id >= 1 && id <= 9) {
			List<Block> res = sudokuBlocks.stream().filter(block -> block.id == id).toList();
			if(!res.isEmpty()) return res.get(0);
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private Block getBlock(Cell cell) {
		List<Block> res = sudokuBlocks.stream().filter(block -> {
			List<Cell> cells = Arrays.asList(block.cells);
			return cells.contains(cell);
		}).toList();
		if(res.isEmpty()) return null;
		else return res.get(0); 
	}

	@SuppressWarnings("unused")
	private Row getRow(int id) {
		if(id >= 1 && id <= 9) {
			List<Row> res = sudokuRows.stream().filter(row -> row.id == id).toList();
			if(!res.isEmpty()) return res.get(0);
		}
		return null;
	}

	@SuppressWarnings("unused")
	private Row getRow(Cell cell) {
		List<Row> res = sudokuRows.stream().filter(row -> {
			List<Cell> cells = Arrays.asList(row.cells);
			return cells.contains(cell);
		}).toList();
		if(res.isEmpty()) return null;
		else return res.get(0); 
	}

	@SuppressWarnings("unused")
	private Column getColumn(int id) {
		if(id >= 1 && id <= 9) {
			List<Column> res = sudokuColums.stream().filter(col -> col.id == id).toList();
			if(!res.isEmpty()) return res.get(0);
		}
		return null;
	}

	@SuppressWarnings("unused")
	private Column getColumns(Cell cell) {
		List<Column> res = sudokuColums.stream().filter(col -> {
			List<Cell> cells = Arrays.asList(col.cells);
			return cells.contains(cell);
		}).toList();
		if(res.isEmpty()) return null;
		else return res.get(0); 
	}

	private boolean isSolved() {
		for(Cell cell : sudokuCells) {
			if(!cell.isValid()) return false;
		}
		
		for(Block block : sudokuBlocks) {
			if(!block.isComplete()) return false;
		}
		
		for(Row row : sudokuRows) {
			if(!row.isComplete()) return false;
		}
		
		for(Column column : sudokuColums) {
			if(!column.isComplete()) return false;
		}
		
		return true;
	}
	
	private static class Cell implements Serializable {

		private static final Color defaultColor = Color.BLACK;
		private static final Color userColor = new Color(51, 93, 178);
		private static final Color errorColor = new Color(237, 23, 36);
		
		private final int x, y;
		private int value = 0;
		private Color color = userColor;
		private boolean finalValue = false;
		private List<Integer> pencils = new ArrayList<>();
		private Color backgroundColor = null;
		
		private Block block = null;
		private Row row = null;
		private Column col = null;
		
		private Cell(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		private boolean isValid() {
			return value >= 1 && value <= 9;
		}
		
		private void addPencil(int value) {
			if(value >= 0 && value <= 9) {
				if(!pencils.contains(value)) pencils.add(value);
			} else throw new IllegalArgumentException("number must be value between 0 and 9");
		}
		
		private void removePencil(int value) {
			if(value >= 0 && value <= 9) {
				if(pencils.contains(value)) {
					pencils = pencils.stream().filter(n -> n != value).collect(Collectors.toList());
				}
			} else throw new IllegalArgumentException("number must be value between 0 and 9");
		}
		
		private void setValue(int value) {
			if(!finalValue) {
				if(value >= 0 && value <= 9) {
					this.value = value;
				} else throw new IllegalArgumentException("number must be value between 0 and 9");
			}
		}
		
		private void setFinal(boolean toggle) {
			this.finalValue = toggle;
			this.color = toggle ? defaultColor : userColor;
		}
		
		private boolean asd() {
			for(Cell cell : getBlock().cells) if(!equals(cell) && value == cell.value) return true;
			for(Cell cell : getRow().cells) if(!equals(cell) && value == cell.value) return true;
			for(Cell cell : getColumn().cells) if(!equals(cell) && value == cell.value) return true;
			return false;
		}

		private Block getBlock() {
			if(block != null) return block;
			List<Block> res = sudoku.sudokuBlocks.stream().filter(block -> {
				List<Cell> cells = Arrays.asList(block.cells);
				return cells.contains(this);
			}).toList();
			block = res.get(0);
			return block;
		}

		private Row getRow() {
			if(row != null) return row;
			List<Row> res = sudoku.sudokuRows.stream().filter(row -> {
				List<Cell> cells = Arrays.asList(row.cells);
				return cells.contains(this);
			}).toList();
			row = res.get(0);
			return row;
		}
		
		private Column getColumn() {
			if(col != null) return col;
			List<Column> res = sudoku.sudokuColums.stream().filter(col -> {
				List<Cell> cells = Arrays.asList(col.cells);
				return cells.contains(this);
			}).toList();
			col = res.get(0);
			return col;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Cell c) {
				return x == c.x && y == c.y && value == c.value;
			}
			return false;
		}
		
	}
	
	private abstract static class CellGroup implements Serializable {
		
		final Cell[] cells;
		final int id;
		
		protected CellGroup(int id, Cell...cells) {
			if(cells.length != 9) throw new IllegalArgumentException("Not enough cells provided for this group");
			this.cells = cells;
			this.id = id;
		}
		
		boolean contains(Cell cell) {
			for(Cell c : cells) if(cell.equals(c)) return true;
			return false;
		}
		
		boolean contains(Cell...cells) {
			for(Cell c : cells) {
				if(!contains(c)) return false;
			}
			return true;
		}

		@SuppressWarnings("unused")
		boolean isInUse(int n) {
			if(n >= 1 && n <= 9) {
				List<Cell> cells = Arrays.asList(this.cells);
				return cells.stream().anyMatch(cell -> cell.value == n);
			} else throw new IllegalArgumentException("Number must be between 0 and 9");
		}

		@SuppressWarnings("unused")
		Cell[] forValue(int val) {
			List<Cell> res = new ArrayList<>();
			for(Cell cell : this.cells) {
				if(cell.value == val) res.add(cell);
			}
			return res.toArray(new Cell[0]);
		}
		
		boolean isComplete() {
			Cell[] cells = new Cell[] {null, null, null, null, null, null, null, null, null};
			for(Cell cell : this.cells) {
				if(cell.isValid()) cells[cell.value - 1] = cell;
			}
			
			for(Cell cell : cells) {
				if(cell == null) return false;
			}
			return true;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof CellGroup g) {
				return id == g.id && contains(g.cells);
			}
			return false;
		}
		
	}
	
	private static class Block extends CellGroup {

		protected Block(int id, Cell[] cells) {
			super(id, cells);
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof Block && super.equals(obj);
		}
		
	}
	
	private static class Row extends Block {

		protected Row(int id, Cell[] cells) {
			super(id, cells);
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof Row && super.equals(obj);
		}
		
	}
	
	private static class Column extends Block {

		protected Column(int id, Cell[] cells) {
			super(id, cells);
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof Column && super.equals(obj);
		}
		
	}

}

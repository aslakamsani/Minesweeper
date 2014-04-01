import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.*;
import javax.swing.event.*;
import java.util.*;
import java.net.URL;

public class Minesweeper{
    public static void main(String[] args){
        MSGUI msGUI = new MSGUI();
    }
}

class MSGUI extends MouseAdapter implements ActionListener{
    int mines, trueMines, time = 0;
    Grid grid;
    JLabel mineLabel, curTime; //show number of mines and time
    //four states: ' ','c','f','?' for blank, clicked, flag, question mark
    char[][] note = new char[20][20];
    int[][] field = new int[20][20]; //0-8, 9 for mine
    javax.swing.Timer timer, autoTimer; //game timer and autoPlay timer
    boolean gameEnd = false, change = false; //game won/lost, update made in autoPlay

    MSGUI(){
        //create GUI
        JFrame window = new JFrame("Minesweeper");
        window.setBounds(100,100,445,600);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //labels at bottom
        mineLabel = new JLabel("Mines: 70");
        mineLabel.setBounds(90,470,125,20);
        curTime = new JLabel("Time Elapsed: 0 secs");
        curTime.setBounds(215,470,150,20);
        
        //initialize board
        for(int i=0; i<note.length; i++)
            for(int j=0; j<note[i].length; j++)
                note[i][j] = ' ';
        initField(70);
        
        //create grid
        grid = new Grid();
        grid.setBounds(20,20,400,400);
        grid.setBorder(BorderFactory.createEtchedBorder());
        grid.addMouseListener(this);
        grid.addMouseMotionListener(this);
        
        //add menu
        JMenuBar menuBar = new JMenuBar();
        JMenu game = new JMenu("Game"); game.setMnemonic('G');
        JMenu option = new JMenu("Options"); option.setMnemonic('O');
        JMenu help = new JMenu("Help"); help.setMnemonic('H');
        
        //sub-menus
        JMenuItem newGame = new JMenuItem("New Game",'N');
        JMenuItem exit = new JMenuItem("Exit",'E');
        JMenuItem mineNum = new JMenuItem("Number of Mines",'N');
        JMenuItem auto = new JMenuItem("Auto Play",'P');
        JMenuItem howTo = new JMenuItem("How to Play",'H');
        JMenuItem about = new JMenuItem("About",'A');
        
        newGame.addActionListener(this);
        exit.addActionListener(this);
        mineNum.addActionListener(this);
        auto.addActionListener(this);
        howTo.addActionListener(this);
        about.addActionListener(this);
        
        game.add(newGame); game.add(exit);
        option.add(mineNum); option.add(auto);
        help.add(howTo); help.add(about);
        menuBar.add(game); menuBar.add(option); menuBar.add(help);
        
        //put things together
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.add(grid);
        mainPanel.add(curTime);
        mainPanel.add(mineLabel);
        
        timer = new javax.swing.Timer(1000,this);
        autoTimer = new javax.swing.Timer(1000,this);
        
        window.setJMenuBar(menuBar);
        window.getContentPane().add(mainPanel);
        window.setVisible(true);
    }
    
    //fills field[][] with numMines mines as well as numbers
    private void initField(int numMines){
        Random rand = new Random();
        for(int i=0; i<numMines; i++){
            int r = rand.nextInt(20);
            int c = rand.nextInt(20);
            if(field[r][c]==9) i--;
            else field[r][c] = 9;
        }
        for(int j=0; j<field.length; j++){
            for(int k=0; k<field[j].length; k++){
                if(field[j][k]==9);
                //how many mines surround this cell?
                else field[j][k] = surroundMines(j,k);
            }
        }
        mines = trueMines = numMines;
    }
    //returns number of mines surrounding a cell
    private int surroundMines(int r,int c){
        int surMines = 0;
        for(int i=r-1; i<=r+1; i++){
            for(int j=c-1; j<=c+1; j++){
                try{
                    if(i==r && j==c);
                    else if(field[i][j]==9) surMines++;
                }catch(Exception e){}
            }
        }
        return surMines;
    }
    
    //clicks all adjacent blank cells, stopping at filled cells
    private int[][] floodClear(int[][] board,int x,int y){
        int[][] copy = (int[][]) board.clone();
        if(x>=0&&x<=19&&y>=0&&y<=19){
            if(copy[y][x]==0&&note[y][x]!='f'&&note[y][x]!='?'){
                copy[y][x] = -1;
                floodClear(copy,x,y-1);
                floodClear(copy,x+1,y);
                floodClear(copy,x,y+1);
                floodClear(copy,x-1,y);
            }
        }
        return copy;
    }
    //clicks numbered cells directly adjacent to blank cells cleared by floodClear()
    private void borders(int x,int y){
        for(int m=y-1; m<=y+1; m++){
            for(int n=x-1; n<=x+1; n++){
                if(m>=0&&m<=19 && n>=0&&n<=19)
                    if(field[m][n]>0&&field[m][n]!=9&&note[m][n]==' ')
                        note[m][n] = 'c';
            }
        }
    }
    
    //left-clicks a cell
    private void leftClick(int x,int y){
        if(note[y][x]!='f'&&note[y][x]!='?'&&field[y][x]!=9){
            note[y][x] = 'c';
            //clear appropriate cells
            field = (int[][]) floodClear(field,x,y).clone();
            for(int i=0; i<field.length; i++){
                for(int j=0; j<field[i].length; j++){
                    if(field[i][j]==-1){
                        borders(j,i);
                        field[i][j] = 0;
                        note[i][j] = 'c';
                    }
                }
            }
        }else if(field[y][x]==9&&note[y][x]!='f'&&note[y][x]!='?'){
            //if a mine is clicked, end the game
            note[y][x] = 'c';
            gameLost();
        }
        grid.repaint();
    }
    //right-clicks a cell
    private void rightClick(int x,int y){
        if(note[y][x]==' '){
            //add a flag
        	note[y][x] = 'f';
            mineLabel.setText("Mines: " + --mines);
        }else if(note[y][x]=='f'){
            //add a question mark
        	note[y][x] = '?';
            mineLabel.setText("Mines: " + ++mines);
        }else if(note[y][x]=='?') note[y][x] = ' ';
        	//keep cell blank
        if(mines<0)
            mineLabel.setText(Math.abs(mines)+" mine(s) too many.");
        grid.repaint();
    }
    
    //resets game, initializes field[][]
    private void reset(int numMines){
        gameEnd = false;
        if(timer.isRunning()) timer.stop();
        time = 0;
        curTime.setText("Time Elapsed: 0 secs");
        mineLabel.setText("Mines: "+numMines);
        for(int i=0; i<note.length; i++){
            for(int j=0; j<note[i].length; j++){
                note[i][j] = ' ';
                field[i][j] = 0;
            }
        }
        initField(numMines);
        grid.repaint();
    }
    //game is won, displays congratulations
    private void gameWon(){
        gameEnd = true;
        timer.stop();
        if(autoTimer.isRunning()) autoTimer.stop();
        JFrame frame = new JFrame();
        String msg = "You won! Click \"New Game\".";
        JOptionPane.showMessageDialog(frame,msg,"Minefield cleared!",
                                      JOptionPane.INFORMATION_MESSAGE);
        grid.repaint();
    }
    //game is lost, message displayed
    private void gameLost(){
        gameEnd = true;
        timer.stop();
        if(autoTimer.isRunning()) autoTimer.stop();
        JFrame frame = new JFrame();
        String msg = "You hit a mine! Click \"New Game\".";
        JOptionPane.showMessageDialog(frame,msg,"Game over",
                                      JOptionPane.INFORMATION_MESSAGE);
        grid.repaint();
    }
    //counts clicked cells to determine a win
    private int countClicked(){
        int count = 0;
        for(int i=0; i<field.length; i++){
            for(int j=0; j<field[i].length; j++){
                if(i>=0&&i<=19&&j>=0&&j<=19)
                    if(field[i][j]!=9&&note[i][j]=='c')
                        count++;
            }
        }
        return count;
    }
    
    //returns number of cells in a specific state surrounding a cell
    private int surround(int r,int c,char n){
        int sur = 0;
        for(int i=r-1; i<=r+1; i++){
            for(int j=c-1; j<=c+1; j++){
                try{
                    if(i==r && j==c);
                    else if(n==' '){
                        if(note[i][j]==' '||note[i][j]=='f') sur++;
                    }else if(n=='f'){
                        if(note[i][j]=='f') sur++;
                    }
                }catch(Exception e){}
            }
        }
        return sur;
    }
    //changes surrounding cells to specified state
    private void fillSurround(int r,int c,char n){
        for(int i=r-1; i<=r+1; i++){
            for(int j=c-1; j<=c+1; j++){
                try{
                    if(i==r&&j==c);
                    else if(n=='c'){
                        if(note[i][j]==' ')
                            leftClick(j,i);
                    }else if(n=='f'){
                        if(note[i][j]==' ')
                            rightClick(j,i);
                    }
                }catch(Exception e){}
            }
        }
    }
    //automatically plays the current game, can be assisted by player
    private void autoPlay(boolean pick){
        Random rand = new Random();
        //randomly click a cell
        if(!gameEnd&&pick) leftClick(rand.nextInt(20),rand.nextInt(20));
        if(change) change = false;
        //if there are as many flagged cells surrounding a particular one
        //as is indicated by it, click all other cells
        //else, do the inverse
        for(int i=0; i<note.length; i++){
            for(int j=0; j<note[i].length; j++){
                if(!gameEnd){
                    if(note[i][j]=='c'&&field[i][j]>0){
                        if(surround(i,j,'f')==field[i][j]){
                            fillSurround(i,j,'c');
                            change = true;
                        }else if(surround(i,j,' ')==field[i][j]){
                            fillSurround(i,j,'f');
                            change = true;
                        }
                    }   
                }
            }
        }
    }
    
    public void actionPerformed(ActionEvent e){
        String command = e.getActionCommand();
        if(e.getSource()==timer){
            curTime.setText("Time Elapsed: "+ ++time +" secs");
            //check if the player has won if the player has marked all mines
            if(mines==0&&!gameEnd)
                if(countClicked()==(400-trueMines)) gameWon();
        }else if(e.getSource()==autoTimer){
            //updates computer's play every second
            if(!gameEnd) autoPlay(!change);
            //check if the player has won if the player has marked all mines
            if(mines==0&&!gameEnd)
                if(countClicked()==(400-trueMines)) gameWon();
        }else if(command.equals("Exit")) System.exit(0);
        else if(command.equals("New Game")) reset(trueMines);
        else if(command.equals("How to Play")){
            //displays How to Play window
            try{
                JEditorPane htp = new JEditorPane(new URL("file:res/help.html"));
                JScrollPane htpPane = new JScrollPane(htp);
                JFrame helpFrame = new JFrame();
                JOptionPane.showMessageDialog(helpFrame,htpPane,"How to Play",
                                              JOptionPane.PLAIN_MESSAGE,null);
            }catch(Exception x){}
        }else if(command.equals("About")){
            //displays About window
            try{
                JEditorPane ver = new JEditorPane(new URL("file:res/about.html"));
                JScrollPane verPane = new JScrollPane(ver);
                JFrame infoFrame = new JFrame();
                JOptionPane.showMessageDialog(infoFrame,verPane,"About Minesweeper",
                                              JOptionPane.PLAIN_MESSAGE,null);
            }catch(Exception x){}
        }else if(command.equals("Number of Mines")){
            //asks the user for a number of mines to reset with
            JFrame frame = new JFrame();
            String msg = "Enter the number of mines to place (1-399):";
            msg = JOptionPane.showInputDialog(frame,msg,"Number of mines",
                                              JOptionPane.INFORMATION_MESSAGE);
            try{
                int num = Integer.parseInt(msg);
                if(num<1||num>399);
                else reset(num);
            }catch(Exception x){}
        }else if(command.equals("Auto Play")){
            //clears grid of '?', then automatically finishes the game
            for(int i=0; i<note.length; i++)
                for(int j=0; j<note[i].length; j++)
                    if(note[i][j]=='?') rightClick(j,i);
            if(!(autoTimer.isRunning())&&!gameEnd) autoTimer.start();
            autoPlay(false);
        }
    }
    public void mouseClicked(MouseEvent e){
        int x = e.getX()/20, y = e.getY()/20;
        //if the timer has not been started and the game has not ended, start
        //the timer
        if(!(timer.isRunning())&&!gameEnd) timer.start();
        if(e.isMetaDown()&&!gameEnd){
            rightClick(x,y);
        }else if(!e.isMetaDown()&&!gameEnd){
            leftClick(x,y);
        }
    }
    public void mouseMoved(MouseEvent e){
        //highlights current cell
        if(!gameEnd){
            grid.repaint();
            int rectX = 0; int rectY = 0;
            for(int i=e.getX(); i>=e.getX()-20; i--)
                if(i%20 == 0) rectX = i;
            for(int j=e.getY(); j>=e.getY()-20; j--)
                if(j%20 == 0) rectY = j;
            grid.highlightCell(rectX,rectY,grid.getGraphics());
        }
    }
    
    //class to help draw grid
    private class Grid extends JPanel{
        public void paintComponent(Graphics g){
            //draw grid with 2D cells
        	g.setColor(Color.BLACK);
            for(int x=0; x<this.getWidth(); x+=20)
                g.drawLine(x,0,x,this.getHeight());
            for(int y=0; y<this.getHeight(); y+=20)
                g.drawLine(0,y,this.getWidth(),y);
            
            //draw cells
            Color bgColor = new Color(190,190,190);
            for(int i=0; i<note.length; i++){
                for(int j=0; j<note[i].length; j++){
                    int x = j*20+1, y = i*20+1;
                    if(note[i][j]==' '){
                        //unclicked cell
                        g.setColor(bgColor);
                        g.fill3DRect(x,y,19,19,true);
                    }else if(note[i][j]=='f'){
                        //flag image
                        try{
                            BufferedImage f = ImageIO.read(new File("res/flag.png"));
                            g.drawImage(f,x+2,y+3,15,15,bgColor,null);
                        }catch(Exception e){}
                    }else if(note[i][j]=='?'){
                        //question mark
                        g.setColor(new Color(190,190,190));
                        g.fill3DRect(x,y,19,19,true);
                        g.setColor(Color.BLACK);
                        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
                        g.drawString("?",x+6,y+14);
                    }else if(note[i][j]=='c'){
                        //clicked cell, draw number if not 0 or 9
                        g.setColor(new Color(120,120,120));
                        g.fill3DRect(x,y,19,19,true);
                        if(field[i][j]<=9&&field[i][j]>0){
                            g.setColor(numberColor(field[i][j]));
                            g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
                            String num = ""+field[i][j];
                            g.drawString(num,x+6,y+14);
                        }
                    }
                }
            }
            if(gameEnd) showMines(g);
        }
        //determines color to use for numbers
        private Color numberColor(int num){
            switch(num){
                case 1: return Color.BLUE;
                case 2: return Color.GREEN;
                case 3: return Color.RED;
                case 4: return new Color(127,0,255); //dark blue
                case 5: return new Color(127,0,0); //brownish
                case 6: return Color.CYAN;
                case 7: return Color.BLACK;
                case 8: return Color.MAGENTA;
            }
            return Color.WHITE;
        }
        //highlights cells called by mouseMoved()
        public void highlightCell(int x,int y,Graphics g){
            g.setColor(new Color(255,255,255));
            g.fill3DRect(x+1,y+1,19,19,true);
        }
        //reveals position of all mines, does not override correctly placed flags
        public void showMines(Graphics g){
            Color bgColor = new Color(190,190,190);
            for(int i=0; i<field.length; i++){
                for(int j=0; j<field[i].length; j++){
                    if(field[i][j]==9&&note[i][j]!='f'&&note[i][j]!='?'){
                        //show unflagged mine
                    	try{
                            int x = j*20, y = i*20;
                            BufferedImage m = ImageIO.read(new File("res/mine.png"));
                            g.drawImage(m,x+4,y+3,15,15,bgColor,null);
                        }catch(Exception e){}
                    }else if(field[i][j]!=9&&(note[i][j]=='f'||note[i][j]=='?')){
                        //show flagged or suspected mine
                    	try{
                            int x = j*20, y = i*20;
                            BufferedImage m = ImageIO.read(new File("res/mine.png"));
                            g.drawImage(m,x+4,y+3,15,15,bgColor,null);
                            g.setColor(new Color(200,0,0));
                            g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,16));
                            g.drawString("X",x+5,y+16);
                        }catch(Exception e){}
                    }
                }
            }
        }
    }
}
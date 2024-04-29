
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;

public class Card extends JLabel implements MouseMotionListener, MouseListener, ComponentListener {

  // Variables estaticas para las dimensiones (Por si se necesita saber de forma externa)
  public static int CARD_HEIGHT = 250, CARD_WIDTH = CARD_HEIGHT/100 * 80;

  // Color de la carta
  private CardColor color;

  // Tipo de carta
  private CardType type;

  // Variables locales del ancho, alto y grosor del borde
  private int width, height, thickness;
  
  // Variable del tiempo global para las animaciones
  private int time = 0;

  // Variables para la Animacion cuando el mouse esta sobre la carta
  private Timer animTimer;
  private int hover_Y = 50, hoverDuration = 15;
  private Point hoverTargetPos;
  private Point originalPos, pos;

  // Variables de audio para efectos de sonido
  private Audio hoverFx;
  private Audio selectFx;

  // Variables para el icono de en medio y para los de las esquinas
  private ImageIcon miniIcon, iconType;
  private JLabel miniTop, miniBot, mid;

  public Card(CardColor color, CardType type) {
    super();
    this.height = CARD_HEIGHT;
    this.width = CARD_WIDTH;
    this.thickness = width/100 * 8; // 8% del ancho

    setPreferredSize(new Dimension(width, height));
    setSize(width, height);
    setLayout(new BorderLayout(0,0));
    setBorder(BorderFactory.createLineBorder(Color.WHITE, thickness, true));

    this.color = color;
    this.type = type;
    this.miniIcon = generarImagen(
      new ImageIcon("iconos/WHITE"+type.ordinal()+".png"),
      getWidth()/6, //16.666% del ancho
      getHeight()/6 // 16.666% del alto
    );

    this.iconType = generarImagen(
      new ImageIcon("iconos/"+color.getColorName()+type.ordinal()+".png"),
      getWidth()*67/100,// 67% del ancho 
      getHeight()*60/100 // 60% del alto
    ); 


    miniTop = new JLabel(miniIcon);
    miniBot = new JLabel(miniIcon);
    mid = new JLabel(iconType);

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));
    top.setOpaque(false);
    top.add(miniTop);
    JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
    bot.setOpaque(false);
    bot.add(miniBot);

    add(top, BorderLayout.NORTH);
    add(mid, BorderLayout.CENTER);
    add(bot, BorderLayout.SOUTH);
    addMouseMotionListener(this);
    addMouseListener(this);
    addComponentListener(this);

    animTimer = new Timer(1, null);
    updateOriginalPos();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    AffineTransform originTransform = g2d.getTransform();

    //Fondo Gradiente
    g2d.setPaint(color.getGradiente(getWidth(), getHeight()));
    g2d.fillRoundRect(5, 5, getWidth()-5, getHeight()-5, thickness, thickness);

    //Ovalo
    int ovalW = getWidth()*90/100; // 90% del ancho
    int ovalH = getHeight()*85/100; // 85% del alto
    g2d.rotate(Math.toRadians(25));
    g2d.translate(getWidth()/3, -getHeight()/10); // 33% del ancho y 10% del alto
    g2d.setColor(Color.WHITE);
    g2d.fillOval(0 , 0 , ovalW, ovalH);

    //Iconos
    miniTop.setIcon(miniIcon);
    miniBot.setIcon(miniIcon);
    mid.setIcon(iconType);

    g2d.setTransform(originTransform);
  }

  public ImageIcon generarImagen(ImageIcon originalIcon, int width, int height) {
    Image originalImg = originalIcon.getImage();
    Image escaladaImg = originalImg.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    return new ImageIcon(escaladaImg);
  }

  public void escalar(double factor) {
    int newWidth = (int) (getWidth() * factor);
    int newHeight = (int) (getHeight() * factor);
    setPreferredSize(new Dimension(newWidth, newHeight));
    setSize(newWidth, newHeight);
    this.miniIcon = generarImagen(
      new ImageIcon("iconos/WHITE"+type.ordinal()+".png"),
      getWidth()/6,
      getHeight()/6);

    this.iconType = generarImagen(
      new ImageIcon("iconos/"+color.getColorName()+type.ordinal()+".png"),
      getWidth()*67/100,
      getHeight()*60/100);
    repaint();
  }

  public static LinkedList<Card> generarBaraja() {
    LinkedList<Card> cartas = new LinkedList<>();
    //Cartas comunes hasta el de Comer
    for(int i = 0; i < CardColor.MAX_COLORS-1; i++) {
      for(double j = 0; j < CardType.valueOf("EAT").ordinal(); j+=0.5) {
        Card card = new Card(new CardColor(i), CardType.values()[(int)j]);
        card.escalar(0.5);
        cartas.add(card);
      }
    }

    //Cartas comodines (Negras)
    for(int i = 0; i < 4; i++ ) {
      Card wild_eat = new Card(new CardColor(CardColor.BLACK), CardType.WILD_EAT);
      wild_eat.escalar(0.5);
      Card wild = new Card(new CardColor(CardColor.BLACK), CardType.WILD);
      wild.escalar(0.5);
      cartas.add(wild_eat);
      cartas.add(wild);
    }
    Collections.shuffle(cartas);
    return cartas;
  }
  
  public static LinkedList<Card> randomCartas(LinkedList<Card> baraja, int max) {
    LinkedList<Card> cartas = new LinkedList<>();
    Random random = new Random();
    ArrayList<Integer> nums = new ArrayList<>();
    int num = 0;
    for(int i = 0; i < max; i++) {
      do {
        num = random.nextInt(baraja.size());
      } while(nums.contains(num));
      nums.add(num);
      Card card = baraja.get(num);
      cartas.add(card);
    }
    return cartas;
  }

  public void updateOriginalPos() { originalPos = getLocation(); }

  synchronized private void hoverAnimacion(Point inicio, Point fin) {
    if(animTimer != null)
      animTimer.stop();
    animTimer = new Timer(1, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(time >= hoverDuration) {
          ((Timer) e.getSource()).stop();
          time = 0;
          return;
        }
        setLocation(Animacion.ease_in(inicio, fin, (double)time/(double)hoverDuration));
        time++;
      }});
      animTimer.start();
  }

  public int getColorInt() { return color.getColorInt(); }
  public CardType getCardType() { return type; }

  public static Comparator<Card> getColorComparator() {
    return new Comparator<Card>() {
      @Override
      public int compare(Card o1, Card o2) {
        return o1.getColorInt() - o2.getColorInt();
      }
    };
  }

  public static Comparator<Card> getTypeComparator() {
    return new Comparator<Card>() {
      @Override
      public int compare(Card o1, Card o2) {
        return o1.getCardType().compareTo(o2.getCardType());
      }
    };
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    Point centro = SwingUtilities.convertPoint(this, e.getPoint(), getParent());
    centro.translate(-getWidth()/2, -getHeight()/2);
    setLocation(centro);
  }

  @Override public void mouseMoved(MouseEvent e) {}
  @Override public void componentResized(ComponentEvent e) {}

  @Override public void componentMoved(ComponentEvent e) {
    pos = getLocation();
  }

  @Override public void componentShown(ComponentEvent e) {}

  @Override public void componentHidden(ComponentEvent e) {}

  @Override public void mouseClicked(MouseEvent e) {
    if(selectFx != null)
      selectFx.stop();
    selectFx = new Audio("sfx/select3.wav");
    selectFx.play();
  }

  @Override public void mousePressed(MouseEvent e) {}

  @Override public void mouseReleased(MouseEvent e) { setLocation(originalPos);}

  @Override
  public void mouseEntered(MouseEvent e) {
    setCursor(new Cursor(Cursor.HAND_CURSOR));
    if(!animTimer.isRunning()) {
      if(hoverFx != null)
        hoverFx.stop();
      hoverFx = new Audio("sfx/hover2.wav");
      hoverFx.play();
      hoverTargetPos = new Point(originalPos.x, originalPos.y - hover_Y);
      hoverAnimacion(originalPos, hoverTargetPos);
    }
  }

  @Override
  public void mouseExited(MouseEvent e) {
    if(!animTimer.isRunning())
      hoverAnimacion(hoverTargetPos, originalPos);
  }

  @Override
  public String toString() {
    return type.toString() + " | " + color.getColorName();
  }
}
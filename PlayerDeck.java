import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PlayerDeck extends JPanel {
  private LinkedList<Card> cartasLista;
  private LinkedList<ArrayList<Card>> cartasDistribuidas;
  private ArrayList<MouseListener> mouseListeners;
  private ArrayList<ComponentListener> componentListeners;
  private int index = -1;

  private JPanel deckPanel;
  private JPanel botonesPanel;

  private JButton izq, der;
  private JButton ordenarColor, ordenarTipo;
  private Font botonesFont = new Font("Consolas", Font.BOLD, 20);
  private Color botonesColor = new Color(255, 120, 0);
  private Border botonesBorder = BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 3, true);
  private Dimension botonesFlechasDimension = new Dimension(90, 230);
  private Dimension botonesOrdenarDimension = new Dimension(30, 30);
  private ImageIcon botonDerIcon = generarImagen(new ImageIcon("iconos/der.png"), botonesFlechasDimension.width,
      botonesFlechasDimension.height);
  private ImageIcon botonDerIconSelected = generarImagen(new ImageIcon("iconos/der_hover.png"),
      botonesFlechasDimension.width, botonesFlechasDimension.height);
  private ImageIcon botonIzqIcon = generarImagen(new ImageIcon("iconos/izq.png"), botonesFlechasDimension.width,
      botonesFlechasDimension.height);
  private ImageIcon botonIzqIconSelected = generarImagen(new ImageIcon("iconos/izq_hover.png"),
      botonesFlechasDimension.width, botonesFlechasDimension.height);

  private Font miniFont = new Font("Consolas", Font.BOLD, 14);

  private Audio ordenarFx, cambiarFx;

  public PlayerDeck(LinkedList<Card> cartasIniciales) {
    super();
    setLayout(new BorderLayout(20, 0));
    setBorder(null);
    setOpaque(false);
    setSize(720, 230);

    deckPanel = new JPanel(null);
    deckPanel.setPreferredSize(new Dimension(720, 200));
    deckPanel.setOpaque(false);
    deckPanel.setBackground(new Color(0, 0, 0, 0));
    deckPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 20), 2, true));

    botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    botonesPanel.setOpaque(false);
    botonesPanel.setBackground(new Color(0, 0, 0, 0));
    botonesPanel.setBorder(null);

    JPanel support = new JPanel(new BorderLayout(0, 0));
    support.setBorder(BorderFactory.createRaisedBevelBorder());
    support.setBackground(new Color(255, 255, 0, 30));

    izq = new JButton(botonIzqIcon);
    izq.setFont(botonesFont);
    izq.setBorder(null);
    izq.setContentAreaFilled(false);
    izq.setPreferredSize(botonesFlechasDimension);
    izq.addActionListener((e) -> {
      cambiarFx = new Audio("sfx/hover1.wav", 0.2f);
      displayPrev5();
      getParent().repaint();
    });
    // Se tiene que hacer esto para repintar el fondo y que se siga viendo
    // transparente (Cosas de Swing xd)
    izq.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        getParent().repaint();
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        izq.setIcon(botonIzqIconSelected);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        izq.setCursor(new Cursor(Cursor.HAND_CURSOR));
        izq.setIcon(botonIzqIcon);
      }
    });

    der = new JButton(botonDerIcon);
    der.setFont(botonesFont);
    der.setBorder(null);
    der.setPreferredSize(botonesFlechasDimension);
    der.setContentAreaFilled(false);
    der.addActionListener((e) -> {
      cambiarFx = new Audio("sfx/hover1.wav", 0.2f);
      displayNext5();
      getParent().repaint();
    });
    // Se tiene que hacer esto para repintar el fondo y que se siga viendo
    // transparente (Cosas de Swing xd)
    der.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        getParent().repaint();
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        der.setCursor(new Cursor(Cursor.HAND_CURSOR));
        der.setIcon(botonDerIconSelected);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        der.setIcon(botonDerIcon);
      }
    });

    ordenarColor = new JButton("C");
    ordenarColor.setFont(miniFont);
    ordenarColor.setBorder(botonesBorder);
    ordenarColor.setBackground(botonesColor);
    ordenarColor.setPreferredSize(botonesOrdenarDimension);
    ordenarColor.addActionListener((e) -> {
      ordenarFx = new Audio("sfx/swap1.wav", 0.8f);
      ordenarColor();
      getParent().repaint();
    });

    // Se tiene que hacer esto para repintar el fondo y que se siga viendo
    // transparente (Cosas de Swing xd)
    ordenarColor.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        getParent().repaint();
      }
    });

    ordenarTipo = new JButton("T");
    ordenarTipo.setFont(miniFont);
    ordenarTipo.setBorder(botonesBorder);
    ordenarTipo.setBackground(botonesColor);
    ordenarTipo.setPreferredSize(botonesOrdenarDimension);
    ordenarTipo.addActionListener((e) -> {
      ordenarFx = new Audio("sfx/swap1.wav", 0.8f);
      ordenarTipo();
      getParent().repaint();
    });
    // Se tiene que hacer esto para repintar el fondo y que se siga viendo
    // transparente (Cosas de Swing xd)
    ordenarTipo.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        getParent().repaint();
      }
    });

    botonesPanel.add(ordenarColor);
    botonesPanel.add(ordenarTipo);

    cartasLista = cartasIniciales;
    cartasDistribuidas = new LinkedList<>();
    mouseListeners = new ArrayList<>();
    componentListeners = new ArrayList<>();

    support.add(deckPanel, BorderLayout.CENTER);
    support.add(botonesPanel, BorderLayout.SOUTH);
    add(support, BorderLayout.CENTER);
    add(izq, BorderLayout.WEST);
    add(der, BorderLayout.EAST);
    distribuir();
    displayNext5();
  }

  public void reset(LinkedList<Card> nuevasCartas) {
    cartasDistribuidas.clear();
    // cartasLista.clear();
    cartasLista = (LinkedList<Card>) nuevasCartas.clone();
    updateListeners();
    distribuir();
    index = -1;
    displayNext5();
  }

  public void addCard(Card card) {
    cartasLista.add(card);
    update();
  }

  public void addCard(LinkedList<Card> cards) {
    for (Card card : cards)
      cartasLista.add(card);
    update();
  }

  public Card removeCard(int i) {
    index = 0;
    Card c = cartasLista.remove(i);
    update();
    return c;
  }

  public void removeCard(Card c) {
    index = 0;
    cartasLista.remove(c);
    update();
  }

  // Agrega un mouseListener a todas las cartas del mazo
  public void addCardMouseListener(MouseListener mouseListener) {
    mouseListeners.add(mouseListener);
    update();
  }

  public void addCardComponentListener(ComponentListener componentListener) {
    componentListeners.add(componentListener);
    update();
  }

  public int getListaCartasSize() {
    System.out.println("CartasListaSize: " + cartasLista.size());
    return cartasLista.size();
  }

  public LinkedList<Card> getCartasLista() {
    return cartasLista;
  }

  // ------------------------------- Busqueda Binaria
  // --------------------------------------
  /*
   * int bajo debe valer 0
   * int alto debe valer la longitud del LinkedList cartasLista
   */
  public int busquedaBinaria(Card cartaBuscar, Comparator<Card> comparator, int bajo, int alto) {
    while (bajo <= alto) {
      int medio = bajo + (alto - bajo) / 2;
      Card cartaMedio = cartasLista.get(medio);

      int comparasion = comparator.compare(cartaBuscar, cartaMedio);

      if (comparasion == 0) {
        return medio;
      } else if (comparasion < 0) {
        alto = medio - 1;
      } else {
        bajo = medio + 1;
      }
    }
    return -1;
  }

  public boolean buscarValida(Card c) {
    for (Card card : cartasLista)
      if (c.isValid(card))
        return true;
    return false;
  }

  private void updateListeners() {
    for (Card c : cartasLista) {
      for (MouseListener ml : mouseListeners) {
        c.removeMouseListener(ml);
        c.addMouseListener(ml);
      }
      for (ComponentListener cl : componentListeners) {
        c.removeComponentListener(cl);
        c.addComponentListener(cl);
      }
    }
  }

  private void update() {
    deckPanel.removeAll();
    if (cartasLista.size() >= 1) {
      updateListeners();
      distribuir();
      int x = 18;
      for (Card card : cartasDistribuidas.get(index)) {
        card.setBounds(x, 50, card.getWidth(), card.getHeight());
        card.updateOriginalPos();
        deckPanel.add(card);
        x += card.getWidth() + 15;
      }
    }
    deckPanel.revalidate();
    deckPanel.repaint();

  }

  private void distribuir() {
    cartasDistribuidas.clear();
    ArrayList<Card> buffer = new ArrayList<>();
    for (int i = 0; i < cartasLista.size(); i++) {
      buffer.add(cartasLista.get(i));
      if (((i + 1) % 5 == 0 && i != 0) || (i + 1) == cartasLista.size()) {
        cartasDistribuidas.add((ArrayList<Card>) buffer.clone());
        buffer.clear();
      }
    }
  }

  private void displayNext5() {
    if (cartasDistribuidas.size() >= 1) {
      index = (index + 1) % cartasDistribuidas.size();
      update();
    }
  }

  private void displayPrev5() {
    if (cartasDistribuidas.size() >= 1) {
      index--;
      if (index < 0)
        index = cartasDistribuidas.size() - 1;
      else
        index = index % cartasDistribuidas.size();
      update();
    }
  }

  // -------------------------- QuickSort Ordenar por Color
  // -------------------------------------------------------------------
  private void ordenarColor() {
    // Collections.sort(cartasLista, Card.getColorComparator());
    quickSort(cartasLista);
    update();
  }

  public void quickSort(LinkedList<Card> list) {
    quickSort(list, 0, list.size() - 1, Card.getColorComparator());
  }

  // QuickSort funciona de manera recursiva
  private void quickSort(LinkedList<Card> list, int low, int high, Comparator<Card> comparator) {
    if (low < high) {
      int pi = partition(list, low, high, comparator);
      quickSort(list, low, pi - 1, comparator);
      quickSort(list, pi + 1, high, comparator);
    }
  }

  /* */
  private int partition(LinkedList<Card> list, int low, int high, Comparator<Card> comparator) {
    Card pivot = list.get(high);
    int i = low - 1;

    for (int j = low; j < high; j++) {
      if (comparator.compare(list.get(j), pivot) <= 0) {
        i++;
        Card temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
      }
    }

    Card temp = list.get(i + 1);
    list.set(i + 1, list.get(high));
    list.set(high, temp);

    return i + 1;
  }

  private void ordenarTipo() {
    // Collections.sort(cartasLista, Card.getTypeComparator());
    shellSort(cartasLista, Card.getTypeComparator());
    update();
  }

  private void shellSort(LinkedList<Card> list, Comparator<Card> comparator) {
    int n = list.size();
    // Obtenemos el tamano del salto diviendo el tamano de la lista ente 2
    for (int salto = n / 2; salto > 0; salto /= 2) {
      // Metodo de insercion con salto
      for (int i = salto; i < n; i++) {
        // Guardar el elemento de lista[i] en un auxiliar;
        Card temp = list.get(i);

        // Shift earlier gap-sorted elements up until the correct location for list[i]
        // is found
        int j;
        /*
         * J inicia siendo el valor del salto
         * hay dos condiciones que se deben cumplir
         * 1. j siempre tiene que ser mayor o igual que el salto sino significa que esta
         * intentando acceder a un
         * indice negativo y se detiene.
         * 2. se compara el valor del la carta en el indice j - salto con el valor aux,
         * si regresa un numero mayor
         * a 0 significa que el valor en el indice (j-salto) es mayor al valor al aux y
         * se intercambian
         * se decrementa el valor de j - salto.
         */
        for (j = i; j >= salto && comparator.compare(list.get(j - salto), temp) > 0; j -= salto) {
          /* lista.get(0) se guarda en el indice del salto */
          list.set(j, list.get(j - salto));
        }

        // J ahora vale j - salto, temp se guarda en ese indice.
        list.set(j, temp);
      }
    }
  }

  private ImageIcon generarImagen(ImageIcon originalIcon, int width, int height) {
    Image originalImg = originalIcon.getImage();
    Image escaladaImg = originalImg.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    return new ImageIcon(escaladaImg);
  }

}

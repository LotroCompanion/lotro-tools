package delta.games.lotro.tools.lore.items.scalables.charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import delta.common.ui.swing.GuiFactory;
import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.ArmourTypes;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.ItemQuality;

/**
 * Controller for armour stat charts.
 * @author DAM
 */
public class ScalableStatChartController
{
  // GUI
  private JPanel _panel;
  private JFreeChart _chart;
  private ArmourType _type;
  private EquipmentLocation _location;
  //private ScaledArmourComputer _computer;

  /**
   * Constructor.
   * @param type Armour type to use.
   * @param location Armour location.
   */
  public ScalableStatChartController(ArmourType type, EquipmentLocation location)
  {
    _type=type;
    _location=location;
    //_computer=new ScaledArmourComputer();
  }

  private String getTitle() {
    String title=_type + " - " + _location;
    return title;
  }

  /**
   * Get the managed panel.
   * @return the managed panel.
   */
  public JPanel getPanel()
  {
    if (_panel==null)
    {
      _panel=buildPanel();
    }
    return _panel;
  }

  private JPanel buildPanel()
  {
    _chart=buildChart();
    _panel=buildChartPanel();
    return _panel;
  }

  private JPanel buildChartPanel()
  {
    ChartPanel chartPanel=new ChartPanel(_chart);
    chartPanel.setDomainZoomable(true);
    chartPanel.setRangeZoomable(true);
    chartPanel.setHorizontalAxisTrace(false);
    chartPanel.setVerticalAxisTrace(false);
    chartPanel.setPreferredSize(new Dimension(500,300));
    chartPanel.setOpaque(false);
    return chartPanel;
  }

  private JFreeChart buildChart()
  {
    String xAxisLabel="Item level";
    String yAxisLabel=getTitle();
    String title=yAxisLabel;

    XYDataset xydataset=createDataset();
    JFreeChart jfreechart=ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, xydataset, PlotOrientation.VERTICAL, true, true, false);

    Color foregroundColor=GuiFactory.getForegroundColor();
    Paint backgroundPaint=GuiFactory.getBackgroundPaint();
    jfreechart.setBackgroundPaint(backgroundPaint);

    TextTitle t=new TextTitle(title);
    t.setFont(t.getFont().deriveFont(24.0f));
    t.setPaint(foregroundColor);
    jfreechart.setTitle(t);

    XYPlot plot = jfreechart.getXYPlot();
    plot.setDomainPannable(false);

    XYToolTipGenerator tooltip=new StandardXYToolTipGenerator() {
      @Override
      public String generateLabelString(XYDataset dataset, int series, int item)
      {
        String name=(String)((XYSeriesCollection)dataset).getSeriesKey(series);
        double value=dataset.getYValue(series,item);
        int level=(int)dataset.getXValue(series,item);
        return name+": "+level+" -> "+value;
      }
    };
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    renderer.setSeriesLinesVisible(0, true);
    renderer.setSeriesShapesVisible(0, true);
    renderer.setBaseToolTipGenerator(tooltip);
    plot.setRenderer(renderer);

    // X axis
    NumberAxis axis = (NumberAxis) plot.getDomainAxis();
    axis.setAxisLinePaint(foregroundColor);
    axis.setLabelPaint(foregroundColor);
    axis.setTickLabelPaint(foregroundColor);

    // Y axis
    NumberAxis valueAxis = (NumberAxis)plot.getRangeAxis();
    valueAxis.setAutoRange(true);
    valueAxis.setAutoRangeIncludesZero(false);
    valueAxis.setAxisLinePaint(foregroundColor);
    valueAxis.setLabelPaint(foregroundColor);
    valueAxis.setTickLabelPaint(foregroundColor);

    LegendTitle legend=jfreechart.getLegend();
    legend.setPosition(RectangleEdge.BOTTOM);
    legend.setItemPaint(foregroundColor);
    legend.setBackgroundPaint(backgroundPaint);
    return jfreechart;
  }

  private XYSeriesCollection createDataset()
  {
    ItemQuality[] qualities={ ItemQuality.UNCOMMON, ItemQuality.RARE, ItemQuality.INCOMPARABLE, ItemQuality.LEGENDARY};
    XYSeriesCollection data=new XYSeriesCollection();
    for(ItemQuality quality : qualities)
    {
      addSeries(data,quality);
    }
    return data;
  }

  private void addSeries(XYSeriesCollection data, ItemQuality quality)
  {
    String key=quality.getMeaning();
    XYSeries toonSeries = new XYSeries(key);
    // 99->105
    int[] levels={ 188, 192, 202, 207, 212, 217 };
    for(int i=0;i<levels.length;i++)
    {
      int level=levels[i];
      double value=1;/*_computer.getArmour(level,_type,_location,quality,1);*/
      toonSeries.add(level,value);
    }
    data.addSeries(toonSeries);
  }

  /**
   * Release all managed resources.
   */
  public void dispose()
  {
    if (_panel!=null)
    {
      _panel.removeAll();
      _panel=null;
    }
    _chart=null;
  }

  /**
   * Main method for this test/tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    ArmourType[] types={ArmourTypes.HEAVY, ArmourTypes.MEDIUM, ArmourTypes.LIGHT};
    EquipmentLocation[] locations={EquipmentLocation.FEET, EquipmentLocation.LEGS, EquipmentLocation.CHEST,
        EquipmentLocation.SHOULDER, EquipmentLocation.HEAD, EquipmentLocation.HAND, EquipmentLocation.BACK
    };
    for(ArmourType type : types)
    {
      for(EquipmentLocation location : locations)
      {
        JPanel panel=GuiFactory.buildBackgroundPanel(new BorderLayout());
        ScalableStatChartController chartController=new ScalableStatChartController(type,location);
        JPanel chartPanel=chartController.getPanel();
        panel.add(chartPanel,BorderLayout.CENTER);
        JFrame frame=new JFrame();
        frame.setTitle(chartController.getTitle());
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
      }
    }
  }
}

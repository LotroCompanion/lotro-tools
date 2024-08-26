package delta.games.lotro.tools.tools.progressions.charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

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
import delta.games.lotro.common.progression.ProgressionsManager;
import delta.games.lotro.utils.maths.ArrayProgression;
import delta.games.lotro.utils.maths.LinearInterpolatingProgression;
import delta.games.lotro.utils.maths.Progression;

/**
 * Controller to display progression charts.
 * @author DAM
 */
public class ProgressionDisplayChartController
{
  // GUI
  private JPanel _panel;
  private JFreeChart _chart;
  // Data
  private int _progressionID;
  private String _name;

  /**
   * Constructor.
   * @param progressionID Progression ID.
   * @param name Name.
   */
  public ProgressionDisplayChartController(int progressionID, String name)
  {
    _progressionID=progressionID;
    _name=name;
  }

  private String getTitle()
  {
    String title=_name+" - "+_progressionID;
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
    String xAxisLabel="X";
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
    renderer.setSeriesShapesVisible(0, false);
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
    XYSeriesCollection data=new XYSeriesCollection();
    XYSeries toonSeries = new XYSeries(String.valueOf(_progressionID));
    Progression progression=ProgressionsManager.getInstance().getProgression(_progressionID);
    if (progression instanceof LinearInterpolatingProgression)
    {
      LinearInterpolatingProgression lip=(LinearInterpolatingProgression)progression;
      int minX=lip.getMinX();
      int maxX=lip.getMaxX();
      for(int i=minX;i<maxX;i++)
      {
        double value=lip.getValue(i).floatValue();
        toonSeries.add(i,value);
      }
    }
    else if (progression instanceof ArrayProgression)
    {
      ArrayProgression ap=(ArrayProgression)progression;
      int nbPoints=ap.getNumberOfPoints();
      int minX=ap.getMinX();
      int maxX=ap.getMinX()+nbPoints-1;
      for(int i=minX;i<=maxX;i++)
      {
        Number value=ap.getRawValue(i);
        toonSeries.add(i,value);
      }
    }
    data.addSeries(toonSeries);
    return data;
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
    int[] progressionIDs = new int[] {
        1879426638,
        1879055446
    };
    String[] names = new String[] {
        "HPS",
        "Armour"
    };
    int i=0;
    for(int progressionID : progressionIDs)
    {
      JPanel panel=GuiFactory.buildBackgroundPanel(new BorderLayout());
      ProgressionDisplayChartController chartController=new ProgressionDisplayChartController(progressionID,names[i]);
      JPanel chartPanel=chartController.getPanel();
      panel.add(chartPanel,BorderLayout.CENTER);
      JFrame frame=new JFrame();
      frame.setTitle(chartController.getTitle());
      frame.getContentPane().add(chartPanel);
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
      i++;
    }
  }
}

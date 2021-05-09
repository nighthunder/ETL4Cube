package br.ufrj.ppgi.greco.kettle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.graphics.Rectangle;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import br.ufrj.ppgi.greco.kettle.plugin.tools.datatable.DataTable;
import br.ufrj.ppgi.greco.kettle.plugin.tools.swthelper.SwtHelper;

public class DataCubeStepDialog extends BaseStepDialog implements
        StepDialogInterface
{

    private DataCubeStepMeta input;
    private SwtHelper swthlp;
    private String dialogTitle;  

    // Variaveis dos widgets
    private TableView wMapTable;
    private TableView wMapTable1;
    private TableView wMapTable2;
    private TableView wPrefixes;
    
    protected String[][] defaultPrefixes = {{ "@base", "http://example.cubeviz.org/datacube/" },
            { "@prefix owl:", "http://www.w3.org/2002/07/owl#" },
            { "@prefix rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#" },
            { "@prefix rdfs:", "http://www.w3.org/2000/01/rdf-schema#" },
            { "@prefix dc:", "http://purl.org/dc/elements/1.1/" },
            { "@prefix skos:", "http://www.w3.org/2004/02/skos/core#" },
            { "@prefix obo:", "http://purl.obolibrary.org/obo#" },
            { "@prefix sdmx_code:", "http://purl.org/linked-data/sdmx/2009/code#" },
            { "@prefix sdmx_dimension:", "http://purl.org/linked-data/sdmx/2009/dimension#" },
            { "@prefix cube:", "http://purl.org/linked-data/cube#" },
            { "@prefix ex:", "http://meu.exemplo/datacube/" },
            { "@prefix exProp:", "http://meu.exemplo/datacube2/properties/" }};
    

    private TextVar unity;
    

    public DataCubeStepDialog(Shell parent, Object stepMeta,
            TransMeta transMeta, String stepname)
    {
        super(parent, (BaseStepMeta) stepMeta, transMeta, stepname);

        input = (DataCubeStepMeta) baseStepMeta;
        swthlp = new SwtHelper(transMeta, this.props);

        // Additional initialization here
        dialogTitle = "DataCube Transformator";
        // ...
    }

    // Cria widgets especificos da janela
    private Control buildContents(Control lastControl,
            ModifyListener defModListener)
    {

        CTabFolder wTabFolder = swthlp.appendTabFolder(shell, lastControl, 90);

        
        
        // Primeira tab (Descrição das Dimensões)
        CTabItem item = new CTabItem(wTabFolder, SWT.NONE);
        item.setText("Definição e descrição das Dimensões");
        Composite cpt = swthlp.appendComposite(wTabFolder, lastControl);
        ColumnInfo[] columns = new ColumnInfo[] {
                new ColumnInfo("Dimensões",
                		ColumnInfo.COLUMN_TYPE_CCOMBO, this.getFields(), true),
                new ColumnInfo("Label da Dimensão (Descrição)",
                        ColumnInfo.COLUMN_TYPE_TEXT),
                new ColumnInfo("URI da Dimensão                          \u00A0",
                        ColumnInfo.COLUMN_TYPE_TEXT),
                new ColumnInfo("URI do tipo da Dimensão (string, float, int...)",
                        ColumnInfo.COLUMN_TYPE_TEXT)};
        wMapTable = swthlp.appendTableView(cpt, null, columns, defModListener,
                98);
        item.setControl(cpt);
         
        
        
        //Segunda tab (Definição das medidas)
        item = new CTabItem(wTabFolder, SWT.NONE);
        item.setText("Definição e descrição das Medidas");
        cpt = swthlp.appendComposite(wTabFolder, lastControl);    
        ColumnInfo[] columns1 = new ColumnInfo[] {
                new ColumnInfo("Medidas",
                        ColumnInfo.COLUMN_TYPE_CCOMBO, this.getFields(), true),
                new ColumnInfo("Label da Medida",
                		ColumnInfo.COLUMN_TYPE_TEXT),
                new ColumnInfo("URI da Medida                            \u00A0",
                        ColumnInfo.COLUMN_TYPE_TEXT),
                new ColumnInfo("URI do Tipo da Medida (string, float, int...)",
                        ColumnInfo.COLUMN_TYPE_TEXT) };
        wMapTable1 = swthlp.appendTableView(cpt, null, columns1, defModListener,
                98);
        item.setControl(cpt);

 
        //Terceira tab (Definição dos vocabuláros)
        item = new CTabItem(wTabFolder, SWT.NONE);
        item.setText("Prefixos");
        ColumnInfo[] columns3 = new ColumnInfo[] {
                new ColumnInfo("Prefixo", ColumnInfo.COLUMN_TYPE_TEXT),
                new ColumnInfo("Namespace", ColumnInfo.COLUMN_TYPE_TEXT) };
        cpt = swthlp.appendComposite(wTabFolder, null);
        wPrefixes = swthlp.appendTableView(cpt, null, columns3, defModListener,
                90);

        swthlp.appendButtonsRow(cpt, wPrefixes, new String[] { "Clear",
                "Defaults" }, new SelectionListener[] { new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
                wPrefixes.removeAll();
                input.setChanged();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        }, new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
                for (String[] row : defaultPrefixes)
                    wPrefixes.add(row);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        } });
        item.setControl(cpt);

        
        //Quarta tab (Descrição da unidade)
        item = new CTabItem(wTabFolder, SWT.NONE);
        item.setText("Descrição da unidade");
        cpt = swthlp.appendComposite(wTabFolder, lastControl); 
        unity = swthlp.appendTextVarRow(cpt,unity, "Descrição da unidade do DataSet (descrever sobre o que ele trata)",defModListener);  
        item.setControl(cpt);
        
        //Quinta tab (Hierarquias)
        item = new CTabItem(wTabFolder, SWT.NONE);
        item.setText("Definição das hierarquias (de -> para: xx is a part_of yy)");
        cpt = swthlp.appendComposite(wTabFolder, lastControl);    
        ColumnInfo[] columns2 = new ColumnInfo[] {
        		new ColumnInfo("Dimensão em que o valor (atributo) está",
                        ColumnInfo.COLUMN_TYPE_CCOMBO, this.getFields(), true),
                new ColumnInfo("Inserir o valor do atributo (\"de\", \"xx\")",
                        ColumnInfo.COLUMN_TYPE_TEXT),
                new ColumnInfo("Inserir uma label para esse atributo (uma descrição)",
                        ColumnInfo.COLUMN_TYPE_TEXT),
                new ColumnInfo("Inserir a qual hierarquia esse atributo pertence (\"para\", \"yy\")",
                        ColumnInfo.COLUMN_TYPE_TEXT)};
        wMapTable2 = swthlp.appendTableView(cpt, null, columns2, defModListener,
                98);
        item.setControl(cpt); 
        		
        
        wTabFolder.setSelection(0);

        // Return the last created control here
        return wTabFolder;
    }
    

    private String[] getFields()
    {
        return getFields(-1);
    }

    private String[] getFields(int type)
    {

        List<String> result = new ArrayList<String>();

        try
        {
            RowMetaInterface inRowMeta = this.transMeta
                    .getPrevStepFields(stepname);

            List<ValueMetaInterface> fields = inRowMeta.getValueMetaList();

            for (ValueMetaInterface field : fields)
            {
                if (field.getType() == type || type == -1)
                    result.add(field.getName());
            }

        }
        catch (KettleStepException e)
        {
            e.printStackTrace();
        }

        return result.toArray(new String[result.size()]);
    }

    // Adiciona listeners para widgets tratarem Enter
    // The will close the window affirmatively when the user press Enter in one
    // of these text input fields
    private void addSelectionListenerToControls(SelectionAdapter lsDef)
    {
    	unity.addSelectionListener(lsDef);
    }

    @Override
    public String open()
    {

        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN
                | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, input);

        // ModifyListener padrao
        ModifyListener lsMod = new ModifyListener()
        {

            @Override
            public void modifyText(ModifyEvent e)
            {
                input.setChanged();
            }
        };
        boolean changed = input.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);

        shell.setText(dialogTitle);

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Adiciona um label e um input text no topo do dialog shell
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText("DataCube Transformation");
        props.setLook(wlStepname);

        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);

        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);

        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);
        Control lastControl = wStepname;

        // Chama metodo que adiciona os widgets especificos da janela
        lastControl = buildContents(lastControl, lsMod);

        // Bottom buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText("OK"); //$NON-NLS-1$
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText("Cancelar"); //$NON-NLS-1$
        setButtonPositions(new Button[] { wOK, wCancel }, margin, lastControl);

        // Add listeners
        lsCancel = new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        };
        lsOK = new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        // It closes the window affirmatively when the user press enter in one
        // of the text input fields
        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };
        wStepname.addSelectionListener(lsDef);
        addSelectionListenerToControls(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        // Populate the data of the controls
        getData();

        // Set the shell size, based upon previous time...
        setSize();

        // Alarga um pouco mais a janela
        Rectangle shellBounds = shell.getBounds();
        shellBounds.width += 5;
        shell.setBounds(shellBounds);
        
        input.setChanged(changed);

        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return stepname;
    }

    private void getData()
    {

        // Recupera dados do StepMeta e adiciona na GUI
		
        try
        {
            wStepname.selectAll();
    		
            unity.setText(Const.NVL(input.getunity(), ""));

        	
        	//TABLE 1
            DataTable<String> table = input.getMapTable();
            DataTable<String>.RowFactory rf = getRowFactory(table);

            for (int i = 0; i < table.size(); i++)
            {
                wMapTable.add(table.getRowRange(i, rf).getRow());
            }
            wMapTable.remove(0);        

            //TABLE 2
            table = input.getMapTable1();
            rf = getRowFactory1(table);
            for (int i = 0; i < table.size(); i++)
            {
                wMapTable1.add(table.getRowRange(i, rf).getRow());
            }
            wMapTable1.remove(0);
            
            //TABLE 5
            table = input.getMapTable2();
            rf = getRowFactory2(table);
            for (int i = 0; i < table.size(); i++)
            {
                wMapTable2.add(table.getRowRange(i, rf).getRow());
            }
            wMapTable2.remove(0);
            
            java.util.List<java.util.List<String>> prefixes = input
                    .getPrefixes();
            if (prefixes != null)
            {
                wPrefixes.removeAll();

                for (java.util.List<String> list : prefixes)
                {
                    wPrefixes.add(list.get(0), list.get(1));
                }

                wPrefixes.remove(0);
            }
            
            
        }
        catch (NullPointerException e)
        {

        }
    }

    protected void cancel()
    {
        stepname = null;
        input.setChanged(changed);
        dispose();
    }

    protected void ok()
    {
    	if (Const.isEmpty(wStepname.getText())) 
			return;

		stepname = wStepname.getText(); // return value

        // Pega dados da GUI e colocar no StepMeta
		
        //Table 3
		try
        {
            input.setPrefixes(getListOfPrefixesFromTableView());
        }
        catch (Exception e)
        {
        }
        
		//Table 4
		input.setunity(unity.getText());
        
		//Table 1
        DataTable<String> table = input.getMapTable();
        table.clear();
        DataTable<String>.RowFactory rf = getRowFactory(table);

        for (int i = 0; i < wMapTable.getItemCount(); i++)
        {
            table.add(rf.newRow(wMapTable.getItem(i)).getFullRow());
        }
        
        
        //Table 2
        table = input.getMapTable1();
        table.clear();
        rf = getRowFactory1(table);

        for (int i = 0; i < wMapTable1.getItemCount(); i++)
        {
            table.add(rf.newRow(wMapTable1.getItem(i)).getFullRow());
        }
        
        
        //Table 5
        table = input.getMapTable2();
        table.clear();
        rf = getRowFactory2(table);

        for (int i = 0; i < wMapTable2.getItemCount(); i++)
        {
            table.add(rf.newRow(wMapTable2.getItem(i)).getFullRow());
        }
        


        // Fecha janela
        dispose();
    }

    private DataTable<String>.RowFactory getRowFactory(DataTable<String> table)
    {
        return table.newRowFactory(
        				DataCubeStepMeta.Field.MAP_TABLE_DIMENSIONS_FIELD_NAME
                                .name(),
                        DataCubeStepMeta.Field.MAP_TABLE_LABELS_FIELD_NAME
                                .name(),
                        DataCubeStepMeta.Field.MAP_TABLE_URI_FIELD_NAME
                                .name(),
                        DataCubeStepMeta.Field.MAP_TABLE_URI_TYPE_FIELD_NAME
                                .name());
    }
    
    
    private DataTable<String>.RowFactory getRowFactory1(DataTable<String> table)
    {
        return table.newRowFactory(
				        DataCubeStepMeta.Field.MAP_TABLE_MEASURE_FIELD_NAME
				                .name(),
				        DataCubeStepMeta.Field.MAP_TABLE_MEASURE_LABEL_FIELD_NAME
				                .name(),
				        DataCubeStepMeta.Field.MAP_TABLE_MEASURE_URI_FIELD_NAME
				                .name(),
				        DataCubeStepMeta.Field.MAP_TABLE_MEASURE_URI_TYPE_FIELD_NAME
				                .name());
    
    }
   
    private DataTable<String>.RowFactory getRowFactory2(DataTable<String> table)
    {
        return table.newRowFactory(
				        DataCubeStepMeta.Field.MAP_TABLE_HIERARCHY_FIELD_NAME
				                .name(),
				        DataCubeStepMeta.Field.MAP_TABLE_HIERARCHY_DE_FIELD_NAME
				                .name(),
				         DataCubeStepMeta.Field.MAP_TABLE_HIERARCHY_LABEL_FIELD_NAME
				                .name(),
				        DataCubeStepMeta.Field.MAP_TABLE_HIERARCHY_PARA_FIELD_NAME
				                .name());
    
    }
    
    private java.util.List<java.util.List<String>> getListOfPrefixesFromTableView()
    {
        ArrayList<java.util.List<String>> prefixes = new ArrayList<java.util.List<String>>();
        for (int iRow = 0; iRow < wPrefixes.getItemCount(); iRow++)
        {
            String[] row = wPrefixes.getItem(iRow);
            prefixes.add(Arrays.asList(row[0], row[1]));
            
        }
        return prefixes;
    }
    
    
}
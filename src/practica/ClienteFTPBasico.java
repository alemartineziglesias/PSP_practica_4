package practica;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class ClienteFTPBasico extends JFrame 
{
	private static final long serialVersionUID = 1L;
	// Campos de la cabecera parte superior
	static JTextField txtServidor = new JTextField();
	static JTextField txtUsuario = new JTextField();
	static JTextField txtDirectorioRaiz = new JTextField();
	// Campos de mensajes parte inferior
	private static JTextField txtArbolDirectoriosConstruido = new JTextField();
	private static JTextField txtActualizarArbol = new JTextField();
	// Botones
	JButton botonCargar = new JButton("Subir fichero");
	JButton botonDescargar = new JButton("Descargar fichero");
	JButton botonBorrar = new JButton("Eliminar fichero");
	JButton botonRenDir = new JButton("Renombrar carpeta");
	JButton botonCreaDir = new JButton("Crear carpeta");
	JButton botonDelDir = new JButton("Eliminar carpeta");
	JButton botonRenombrar = new JButton("Renombrar fichero");
	JButton botonSalir = new JButton("Salir");
	// Lista para los datos del directorio
	static JList<String> listaDirec = new JList<String>();
	// contenedor
	private final Container c = getContentPane();
	// Datos del servidor FTP - Servidor local
	static FTPClient cliente = new FTPClient();// cliente FTP
	String servidor = "localhost";
	String user = "Alejandro";
	String pasw = "Mr9+agar";
	boolean login;
	static String direcInicial = "/";
	// para saber el directorio y fichero seleccionado
	static String direcSelec = direcInicial;
	static String ficheroSelec = "";
	public static void main(String[] args) throws IOException 
	{
		new ClienteFTPBasico();
	} // final del main

	public ClienteFTPBasico() throws IOException
	{
		super("CLIENTE BÁSICO FTP");
		//para ver los comandos que se originan
		cliente.addProtocolCommandListener(new PrintCommandListener(new PrintWriter (System.out)));
		cliente.connect(servidor); //conexión al servidor
		cliente.enterLocalPassiveMode();
		login = cliente.login(user, pasw);
		//Se establece el directorio de trabajo actual
		cliente.changeWorkingDirectory(direcInicial);
		//Obteniendo ficheros y directorios del directorio actual
		FTPFile[] files = cliente.listFiles();
		llenarLista(files, direcInicial);
		//Construyendo la lista de ficheros y directorios
		//del directorio de trabajo actual		
		//preparar campos de pantalla
		txtArbolDirectoriosConstruido.setText("<< ARBOL DE DIRECTORIOS CONSTRUIDO >>");
		txtServidor.setText("Servidor FTP: " + servidor);
		txtUsuario.setText("Usuario: " + user);
		txtDirectorioRaiz.setText("DIRECTORIO RAIZ: " + direcInicial);
		//Preparación de la lista
		//se configura el tipo de selección para que solo se pueda
		//seleccionar un elemento de la lista

		listaDirec.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//barra de desplazamiento para la lista
		JScrollPane barraDesplazamiento = new JScrollPane(listaDirec);
		barraDesplazamiento.setPreferredSize(new Dimension(335, 420));
		barraDesplazamiento.setBounds(new Rectangle(5, 65, 335, 420));
		JPanel panelBotones = new JPanel(new GridLayout(3, 4, 5, 5));
		c.add(barraDesplazamiento);
		panelBotones.add(txtServidor);
		c.add(txtUsuario);
		panelBotones.add(txtDirectorioRaiz);
		panelBotones.add(txtArbolDirectoriosConstruido);
		panelBotones.add(txtActualizarArbol);
		panelBotones.add(botonCargar);
		panelBotones.add(botonDescargar);
		panelBotones.add(botonBorrar);
		panelBotones.add(botonRenDir);
		panelBotones.add(botonCreaDir);
		panelBotones.add(botonDelDir);
		panelBotones.add(botonRenombrar);
		panelBotones.add(botonSalir);
		c.add(panelBotones);
		c.setLayout(new GridLayout(2, 1));
		//se añaden el resto de los campos de pantalla
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		setSize(1150, 570);
		setVisible(true);
		//Acciones al pulsar en la lista o en los botones
		listaDirec.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent lse)
			{
				String fic = "";
				if (lse.getValueIsAdjusting()) 
				{
					ficheroSelec = "";
					//elemento que se ha seleccionado de la lista
					fic = listaDirec.getSelectedValue().toString();
					//Se trata de un fichero
					ficheroSelec = direcSelec;
					txtArbolDirectoriosConstruido.setText("FICHERO SELECCIONADO: " + ficheroSelec);
					ficheroSelec = fic;//nos quedamos con el nocmbre
					txtActualizarArbol.setText("DIRECTORIO ACTUAL: " + direcSelec);
				}
			}
		});
		listaDirec.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mouseClicked(MouseEvent evt) 
			{
				if (evt.getClickCount() == 2) 
				{
					String seleccionado = listaDirec.getSelectedValue();
					if (seleccionado == null) return;
					try 
					{
						if (seleccionado.equals("(Volver ..)")) 
						{
							// Obtener directorio padre
							cliente.changeToParentDirectory();
							direcSelec = cliente.printWorkingDirectory();
						} 
						else if (seleccionado.startsWith("(DIR) ")) 
						{
							// Si es un directorio, entrar en él
							String nuevoDirectorio = seleccionado.substring(6);
							String directorioActualizado; 
							if (direcSelec.equals("/")) 
							{
								directorioActualizado = "/" + nuevoDirectorio;
							} 
							else 
							{
								directorioActualizado = direcSelec + "/" + nuevoDirectorio;
							}
							cliente.changeWorkingDirectory(directorioActualizado);
							direcSelec = directorioActualizado;
						}

						// Actualizar la lista con los archivos del nuevo directorio
						FTPFile[] files = cliente.listFiles();
						llenarLista(files, direcSelec);
						txtActualizarArbol.setText("DIRECTORIO ACTUAL: " + direcSelec);
					} 
					catch (IOException e) 
					{
						JOptionPane.showMessageDialog(null, "Error al cambiar de directorio.");
						e.printStackTrace();
					}
				}
			}
		});
		botonSalir.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try 
				{
					cliente.disconnect();
				}
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		botonCreaDir.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String nombreCarpeta = JOptionPane.showInputDialog(null, "Introduce el nombre del directorio", "carpeta");
				if (!(nombreCarpeta == null)) 
				{
					String directorio = direcSelec;
					if (!direcSelec.equals("/"))
						directorio = directorio + "/";
					//nombre del directorio a crear
					directorio += nombreCarpeta.trim(); 
					//quita blancos a derecha y a izquierda
					try 
					{
						if (cliente.makeDirectory(directorio))
						{
							String m = nombreCarpeta.trim()+ " => Se ha creado correctamente ...";
							JOptionPane.showMessageDialog(null, m);
							txtArbolDirectoriosConstruido.setText(m);
							//directorio de trabajo actual
							cliente.changeWorkingDirectory(direcSelec);
							FTPFile[] ff2 = null;
							//obtener ficheros del directorio actual
							ff2 = cliente.listFiles();
							//llenar la lista
							llenarLista(ff2, direcSelec);
						}
						else
						{
							JOptionPane.showMessageDialog(null, nombreCarpeta.trim() + " => No se ha podido crear ...");
						}	
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				} // final del if
			}
		}); // final del botón CreaDir
		botonDelDir.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String nombreCarpeta = JOptionPane.showInputDialog(null, "Introduce el nombre del directorio a eliminar", "carpeta");
				if (!(nombreCarpeta == null)) 
				{
					String directorio = direcSelec;
					if (!direcSelec.equals("/"))
					{
						directorio = directorio + "/";
					}
					//nombre del directorio a eliminar
					directorio += nombreCarpeta.trim(); //quita blancos a derecha y a izquierda
					try 
					{
						if(cliente.removeDirectory(directorio)) 
						{
							String m = nombreCarpeta.trim()+" => Se ha eliminado correctamente ...";
							JOptionPane.showMessageDialog(null, m);
							txtArbolDirectoriosConstruido.setText(m);
							//directorio de trabajo actual
							cliente.changeWorkingDirectory(direcSelec);
							FTPFile[] ff2 = null;
							//obtener ficheros del directorio actual
							ff2 = cliente.listFiles();
							//llenar la lista
							llenarLista(ff2, direcSelec);
						}
						else
						{
							JOptionPane.showMessageDialog(null, nombreCarpeta.trim() + " => No se ha podido eliminar ...");
						}
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				} 
				// final del if
			}
		}); 
		//final del botón Eliminar Carpeta
		botonRenombrar.addActionListener(new ActionListener() 
		{
		    @Override
		    public void actionPerformed(ActionEvent e) 
		    {
		        String seleccionado = listaDirec.getSelectedValue();
		        if (seleccionado == null || seleccionado.startsWith("(DIR) ")) 
		        {
		            JOptionPane.showMessageDialog(null, "Debe seleccionar un fichero para renombrar.");
		            return;
		        }
		        // Extraer el nombre y la extensión
		        int puntoIndex = seleccionado.lastIndexOf(".");
		        String nombreSinExtension;
		        String extension;
		        if (puntoIndex == -1) 
		        {
		            nombreSinExtension = seleccionado;
		            extension = ""; // No tiene extensión
		        } 
		        else 
		        {
		            nombreSinExtension = seleccionado.substring(0, puntoIndex);
		            extension = seleccionado.substring(puntoIndex); // Incluye el punto "."
		        }
		        // Pedir al usuario el nuevo nombre sin la extensión
		        String nuevoNombreBase = JOptionPane.showInputDialog(null, "Ingrese el nuevo nombre para el fichero:", nombreSinExtension);
		        if (nuevoNombreBase == null || nuevoNombreBase.trim().isEmpty()) 
		        {
		            return; // Si el usuario cancela o deja vacío, salir.
		        }
		        // Construir el nuevo nombre con la misma extensión
		        String nuevoNombre = nuevoNombreBase.trim() + extension;
		        try 
		        {
		            String rutaActual = direcSelec + "/" + seleccionado;
		            String nuevaRuta = direcSelec + "/" + nuevoNombre;
		            if (cliente.rename(rutaActual, nuevaRuta)) 
		            {
		                JOptionPane.showMessageDialog(null, "Fichero renombrado con éxito.");
		                // Actualizar la lista
		                FTPFile[] files = cliente.listFiles();
		                llenarLista(files, direcSelec);
		            } 
		            else 
		            {
		                JOptionPane.showMessageDialog(null, "No se pudo renombrar el fichero.");
		            }
		        } 
		        catch (IOException ex) 
		        {
		            ex.printStackTrace();
		            JOptionPane.showMessageDialog(null, "Error al renombrar el fichero.");
		        }
		    }
		});
		botonCargar.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser f;
				File file;
				f = new JFileChooser();
				//solo se pueden seleccionar ficheros
				f.setFileSelectionMode(JFileChooser.FILES_ONLY);
				//título de la ventana
				f.setDialogTitle("Selecciona el fichero a subir al servidor FTP");
				//se muestra la ventana
				int returnVal = f.showDialog(f, "Cargar");
				if (returnVal == JFileChooser.APPROVE_OPTION) 
				{
					//fichero seleccionado
					file = f.getSelectedFile();
					//nombre completo del fichero
					String archivo = file.getAbsolutePath();
					//solo nombre del fichero
					String nombreArchivo = file.getName();
					try 
					{
						SubirFichero(archivo, nombreArchivo);
					}
					catch (IOException e1) 
					{
						e1.printStackTrace(); 
					}
				}
			}
		}); //Fin botón subir
		botonDescargar.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String directorio = direcSelec;
				if (!direcSelec.equals("/"))
				{
					directorio = directorio + "/";
				}	
				if (!direcSelec.equals("")) 
				{
					DescargarFichero(directorio + ficheroSelec, ficheroSelec);
				}
			}
		}); // Fin botón descargar
		botonBorrar.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String directorio = direcSelec;
				if (!direcSelec.equals("/"))
				{
					directorio = directorio + "/";
				}	
				if (!direcSelec.equals("")) 
				{
					BorrarFichero(directorio + ficheroSelec, ficheroSelec);
				}
			}
		});
		botonRenDir.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				String seleccionado = listaDirec.getSelectedValue();
				if (seleccionado == null || !seleccionado.startsWith("(DIR) ")) 
				{
					JOptionPane.showMessageDialog(null, "Debe seleccionar una carpeta para renombrar.");
					return;
				}
				// Extraer el nombre real de la carpeta
				String nombreActual = seleccionado.substring(6);
				String nuevoNombre = JOptionPane.showInputDialog(null, "Ingrese el nuevo nombre para la carpeta:", nombreActual);
				if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) 
				{
					return; // Si el usuario cancela o no introduce nombre, salir.
				}
				try 
				{
					String rutaActual = direcSelec + "/" + nombreActual;
					String nuevaRuta = direcSelec + "/" + nuevoNombre.trim();
					if (cliente.rename(rutaActual, nuevaRuta)) 
					{
						JOptionPane.showMessageDialog(null, "Carpeta renombrada con éxito.");
						// Actualizar la lista
						FTPFile[] files = cliente.listFiles();
						llenarLista(files, direcSelec);
					} 
					else 
					{
						JOptionPane.showMessageDialog(null, "No se pudo renombrar la carpeta.");
					}
				} 
				catch (IOException ex) 
				{
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, "Error al renombrar la carpeta.");
				}
			}
		});
	} // fin constructor
	private static void llenarLista(FTPFile[] files, String direc2) 
	{
		if (files == null)
		{
			return;
		}
		DefaultListModel<String> modeloLista = new DefaultListModel<>();
		listaDirec.setForeground(Color.blue);
		Font fuente = new Font("Courier", Font.PLAIN, 12);
		listaDirec.setFont(fuente);
		listaDirec.removeAll();
		try 
		{
			cliente.changeWorkingDirectory(direc2);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		direcSelec = direc2;

		// Si no estamos en el directorio raíz, agregamos la opción "Volver .."
		if (!direcSelec.equals("/")) 
		{
			modeloLista.addElement("(Volver ..)");
		}

		// Agregar archivos y directorios al modelo de lista
		for (FTPFile file : files) 
		{
			if (!(file.getName()).equals(".") && !(file.getName()).equals("..")) 
			{
				String f = file.getName();
				if (file.isDirectory()) f = "(DIR) " + f;
				{
					modeloLista.addElement(f);
				}
			}
		}
		try 
		{
			listaDirec.setModel(modeloLista);
		} 
		catch (NullPointerException n) 
		{
			// Se ignora la excepción
		}
	}
	private boolean SubirFichero(String archivo, String soloNombre) throws IOException 
	{
		cliente.setFileType(FTP.BINARY_FILE_TYPE);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(archivo));
		boolean ok = false;
		//directorio de trabajo actual
		cliente.changeWorkingDirectory(direcSelec);
		if (cliente.storeFile(soloNombre, in)) 
		{
			String s = " " + soloNombre + " => Subido correctamente...";
			txtArbolDirectoriosConstruido.setText(s);
			txtActualizarArbol.setText("Se va a actualizar el árbol de directorios...");
			JOptionPane.showMessageDialog(null, s);
			FTPFile[] ff2 = null;
			//obtener ficheros del directorio actual
			ff2 = cliente.listFiles();
			//llenar la lista con los ficheros del directorio actual
			llenarLista(ff2,direcSelec);
			ok = true;
		}
		else
		{
			txtArbolDirectoriosConstruido.setText("No se ha podido subir... " + soloNombre);
		}	
		return ok;
	}// final de SubirFichero
	private void DescargarFichero(String NombreCompleto, String nombreFichero) 
	{
		File file;
		String archivoyCarpetaDestino = "";
		String carpetaDestino = "";
		JFileChooser f = new JFileChooser();
		//solo se pueden seleccionar directorios
		f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//título de la ventana
		f.setDialogTitle("Selecciona el Directorio donde Descargar el Fichero");
		int returnVal = f.showDialog(null, "Descargar");
		if (returnVal == JFileChooser.APPROVE_OPTION) 
		{
			file = f.getSelectedFile();
			//obtener carpeta de destino
			carpetaDestino = (file.getAbsolutePath()).toString();
			//construimos el nombre completo que se creará en nuestro disco
			archivoyCarpetaDestino = carpetaDestino + File.separator + nombreFichero;
			try 
			{
				cliente.setFileType(FTP.BINARY_FILE_TYPE);
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(archivoyCarpetaDestino));
				if (cliente.retrieveFile(NombreCompleto, out))
				{
					JOptionPane.showMessageDialog(null,	nombreFichero + " => Se ha descargado correctamente ...");
				}
				else
				{
					JOptionPane.showMessageDialog(null,	nombreFichero + " => No se ha podido descargar ...");
				}	
				out.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	} // Final de DescargarFichero
	private void BorrarFichero(String NombreCompleto, String nombreFichero) 
	{
		//pide confirmación
		int seleccion = JOptionPane.showConfirmDialog(null, "¿Desea eliminar el elemento seleccionado?");
		if (seleccion == JOptionPane.OK_OPTION) 
		{
			try 
			{
				if (cliente.deleteFile(NombreCompleto)) 
				{
					String m = nombreFichero + " => Eliminado correctamente... ";
					JOptionPane.showMessageDialog(null, m);
					txtArbolDirectoriosConstruido.setText(m);
					//directorio de trabajo actual
					cliente.changeWorkingDirectory(direcSelec);
					FTPFile[] ff2 = null;
					//obtener ficheros del directorio actual
					ff2 = cliente.listFiles();
					//llenar la lista con los ficheros del directorio actual
					llenarLista(ff2, direcSelec);
				}
				else
				{
					JOptionPane.showMessageDialog(null, nombreFichero + " => No se ha podido eliminar ...");
				}	
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}// Final de BorrarFichero
}// Final de la clase ClienteFTPBasico
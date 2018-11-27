package com.describer.context;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.event.EventListenerList;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.describer.util.Utils;

public class Context {

	public enum TYPE {
		ENTITY, REQUISITE, KEY, MM
	};

	private static String DEFAULT_PROP_RES_STR = "/com/describer/resources/xmlPropsNames.xml";
	private static String BDDESCR_RES_STR = "/com/describer/resources/bddescr.xsd";
	private Set<Entity> entities = new HashSet<Entity>();
	private Set<Requisite> requisites = new HashSet<Requisite>();
	private Set<Key> keys = new HashSet<Key>();
	private Set<MM> mMs = new HashSet<MM>();
	private Properties xmlTagNames = new Properties();
	private EventListenerList listenerList = new EventListenerList();

	//	
	// ctor
	//

	public Context() {
		java.net.URL propsURL = Context.class.getResource(DEFAULT_PROP_RES_STR);
		try {
			loadXmlTagNames(propsURL.openStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//
	// elements creators
	//

	// entity
	public Entity createEntity() {
		Entity ent = createEntityPr();
		fireContextElementAdded(ent);
		return ent;
	}

	protected Entity createEntityPr() {
		return createEntityPr(getFreeId(TYPE.ENTITY), getUniqueName(
				TYPE.ENTITY, getDefNewName(TYPE.ENTITY)), "");
	}

	protected Entity createEntityPr(int id, String name, String semantics) {
		Entity ent = new Entity();
		ent.setCtxPr(this);
		if (!setIdPr(ent, id))
			return null;
		ent.setIdPr(id);
		ent.setNamePr(getUniqueName(TYPE.ENTITY, name));
		ent.setSemanticsPr(semantics);
		entities.add(ent);
		return ent;
	}

	// requisite
	public Requisite createRequisite() {
		Requisite req = createRequisitePr();
		fireContextElementAdded(req);
		return req;
	}

	protected Requisite createRequisitePr() {
		return createRequisitePr(getFreeId(TYPE.REQUISITE), getUniqueName(
				TYPE.REQUISITE, getDefNewName(TYPE.REQUISITE)), "", false);
	}

	protected Requisite createRequisitePr(int id, String name,
			String semantics, boolean required) {
		Requisite req = new Requisite();
		req.setCtxPr(this);
		req.setIdPr(id);
		if (!setIdPr(req, id))
			return null;
		req.setNamePr(getUniqueName(TYPE.REQUISITE, name));
		req.setSemanticsPr(semantics);
		req.setRequiredPr(required);
		requisites.add(req);
		return req;
	}

	// mM
	public MM createMM() {
		MM mM = createMMPr();
		fireContextElementAdded(mM);
		return mM;
	}

	protected MM createMMPr() {
		return createMMPr(getFreeId(TYPE.MM));
	}

	protected MM createMMPr(int id) {
		MM mM = new MM();
		mM.setCtxPr(this);
		if (!setIdPr(mM, id))
			return null;
		mMs.add(mM);
		return mM;
	}

	// key
	/**
	 * @param entity
	 *            - entity that owns this key
	 */
	public Key createKey(Entity entity) {
		Key key = createKeyPr(entity);
		fireContextElementAdded(key);
		fireContextElementChanged(key.getEntity());
		return key;
	}

	protected Key createKeyPr(Entity entity) {
		return createKeyPr(getFreeId(TYPE.KEY), entity);
	}

	protected Key createKeyPr(int id, Entity entity) {
		Key key = new Key();
		key.setCtxPr(this);
		if (!setIdPr(key, id))
			return null;
		if (!isMy(entity))
			return null;
		key.setEntityPr(entity);
		entity.addKeyHasPr(key);
		keys.add(key);
		return key;
	}

	//
	// elements modifiers
	//

	public boolean setId(ContextElement element, int id) {
		boolean result = setIdPr(element, id);
		if (result)
			fireContextElementChanged(element);
		return result;
	}

	protected boolean setIdPr(ContextElement element, int id) {
		boolean result = true;
		for (Iterator<? extends ContextElement> it1 = collectionByType(
				element.getType()).iterator(); it1.hasNext();) {
			if (it1.next().getId() == id) {
				result = false;
			}
		}
		if (result)
			element.setIdPr(id);
		return result;
	}

	private Collection<? extends ContextElement> collectionByType(TYPE type) {
		switch (type) {
		case ENTITY:
			return entities;
		case REQUISITE:
			return requisites;
		case KEY:
			return keys;
		case MM:
			return mMs;
		default:
			assert false;
			return null;
		}
	}

	public boolean setName(ContextElement el, String name) {
		boolean result = setNamePr(el, name);
		if (result)
			fireContextElementChanged(el);
		return result;
	}

	protected boolean setNamePr(ContextElement el, String name) {
		if (el.isNode()) {
			if (((Node) el).getName() == null
					|| !((Node) el).getName().equals(name.trim())) {
				// we change the name only if an element hasn't a name yet or
				// if the new name differs from the current name
				((Node) el).setNamePr(getUniqueName(el.getType(), name));
				return true;
			}
		}
		return false;
	}

	public boolean setSemantics(ContextElement el, String semantics) {
		boolean result = setSemanticsPr(el, semantics);
		if (result)
			fireContextElementChanged(el);
		return result;
	}

	protected boolean setSemanticsPr(ContextElement el, String semantics) {
		if (el.isNode()) {
			((Node) el).setSemanticsPr(semantics);
			return true;
		}
		return false;
	}

	public boolean setRequired(ContextElement el, boolean required) {
		boolean result = setRequiredPr(el, required);
		if (result)
			fireContextElementChanged(el);
		return result;
	}

	protected boolean setRequiredPr(ContextElement el, boolean required) {
		if (el.isRequisite()) {
			((Requisite) el).setRequiredPr(required);
			return true;
		}
		return false;
	}

	public boolean convertNode(ContextElement el) {
		if (!isMy(el))
			return false;
		if (el.isNode() && el.getDirectDependants().isEmpty()) {

			Node oldNode = (Node) el;
			Node newNode;

			Set<Entity> parents = oldNode.getParents();
			Set<Key> keysIn = oldNode.getKeysIn();
			Set<MM> mMs = oldNode.getMMs();
			String name = oldNode.getName();

			// Pay attention, that we need to get new unique name
			// before creating new node
			// because new node will occupy one default name on creation
			if (oldNode.isEntity()) {
				name = getUniqueName(TYPE.REQUISITE, name);
				newNode = createRequisitePr();
			} else if (oldNode.isRequisite()) {
				name = getUniqueName(TYPE.ENTITY, name);
				newNode = createEntityPr();
			} else {
				return false;
			}

			// change name
			newNode.setNamePr(name);

			// change semantics
			newNode.setSemanticsPr(oldNode.getSemantics());

			// change parents
			newNode.addParentsPr(parents);
			for (Entity parent : parents) {
				parent.addChildPr(newNode);
			}

			// change keysIn
			newNode.addKeysInPr(keysIn);
			for (Key key : keysIn) {
				key.addKeyElementPr(newNode);
			}

			// change MM
			newNode.addMMsPr(mMs);
			for (MM mM : mMs) {
				mM.addMMElementPr(newNode);
			}

			if (!removePr(oldNode))
				return false;

			fireContextElementConverted(oldNode, newNode);

			return true;
		}
		return false;
	}

	public boolean remove(ContextElement el) {
		/*
		 * boolean result = removePr(el); if (result) {
		 * fireContextElementRemoved(el); Collection<ContextElement> related =
		 * el.getRelatedElements(); if (!related.isEmpty())
		 * fireContextElementsChanged(related); }
		 */
		return removeAll(java.util.Collections.singleton(el));
	}

	public boolean removeAll(Collection<? extends ContextElement> elems) {

		Set<ContextElement> removed = new HashSet<ContextElement>();
		Set<ContextElement> changed = new HashSet<ContextElement>();

		boolean result = true;

		if (elems.isEmpty())
			result = false;

		for (ContextElement el : elems) {
			if (el.isEntity())
				removed.addAll(((Entity) el).getKeysHas());
			boolean res = removePr(el);
			if (res) {
				removed.add(el);
				changed.addAll(el.getRelatedElements());
			} else
				result = false;
		}
		if (!removed.isEmpty())
			fireContextElementsRemoved(removed);
		changed.removeAll(removed);
		if (!changed.isEmpty())
			fireContextElementsChanged(changed);
		return result;
	}
	
	public boolean removeKeyElements(Key keyEl, Collection<Node> elems) {

		if (keyEl == null 
				|| elems == null)
			throw new NullPointerException();
		
		Key key = (Key) keyEl;
		
		boolean result = key.removeKeyElementsPr(elems);
		
		Set<ContextElement> changed = new HashSet<ContextElement>();
		
		if (result) {
			changed.add(key);
			changed.add(key.getEntity());
			fireContextElementsChanged(changed);
		}
		
		return result;
	}

	protected boolean removePr(ContextElement el) {
		if (el == null) {
			return false;
		}
		if (el.getCtx() != this) {
			return false;
		}
		if (el.isEntity()) {
			Entity ent = (Entity) el;
			// Delete from keysIn
			for (Key key : ent.getKeysIn()) {
				key.removeKeyElementPr(ent);
			}
			// Delete keysHas
			for (Key key : ent.getKeysHas()) {
				if (!removePr(key))
					return false;
			}
			// Delete from parents
			for (Entity e : ent.getParents()) {
				e.removeChildPr(ent);
			}
			// Delete from children
			for (Node nd : ent.getChildren()) {
				nd.removeParentPr(ent);
			}
			// Delete from MMs
			for (MM mM : ent.getMMs()) {
				mM.removeMMElementPr(ent);
			}
			// Delete itself
			entities.remove(ent);
			return true;
		} else if (el.isRequisite()) {
			Requisite req = (Requisite) el;
			// Remove from MMs
			for (MM mM : req.getMMs()) {
				mM.removeMMElementPr(req);
			}
			// Remove from keysIn
			for (Key key : req.getKeysIn()) {
				key.removeKeyElementPr(req);
			}
			// Remove from parents
			for (Entity ent : req.getParents()) {
				ent.removeChildPr(req);
			}
			// Remove itself
			requisites.remove(req);
			return true;
		} else if (el.isKey()) {
			Key key = (Key) el;
			// from keyElements
			for (Node nd : key.getKeyElements()) {
				nd.removeKeyInPr(key);
				// needed for some hooks
				key.removeKeyElementPr(nd);
			}
			// from entity
			key.getEntity().removeKeyHasPr(key);
			// itself
			keys.remove(key);
			return true;
		} else if (el.isMM()) {
			MM mM = (MM) el;
			// from nodes
			for (Node nd : mM.getMMElements()) {
				nd.removeMMPr(mM);
			}
			mMs.remove(mM);
			return true;
		}
		return false;
	}

	public boolean connect(ContextElement el1, ContextElement el2) {
		Set<ContextElement> changed = new HashSet<ContextElement>();
		boolean result = false;
		if (el1.isEntity() && el2.isNode()) {
			// Entity -> Requisite
			// Entity -> Entity
			((Entity) el1).addChildPr((Node) el2);
			((Node) el2).addParentPr((Entity) el1);
			result = true;
		} else if (el1.isNode() && (el2.isMM() || el2.isKey())) {
			if (el2.isMM()) {
				// Node -> MM
				((MM) el2).addMMElementPr((Node) el1);
				((Node) el1).addMMPr((MM) el2);
				result = true;
			} else if (el2.isKey()) {
				// Node -> Key
				if (((Key) el2).getEntity() == el1) {
					result = false;
				} else {
					// el2 - Key; el1 - Node
					(((Key) el2).getEntity()).addChildPr((Node) el1);
					((Node) el1).addParentPr(((Key) el2).getEntity());
					((Key) el2).addKeyElementPr((Node) el1);
					((Node) el1).addKeyInPr((Key) el2);
					changed.add(((Key) el2).getEntity());
					result = true;
				}
			}
		} else if (el2.isNode() && (el1.isMM() || el1.isKey())) {
			// MM -> Node
			// Key -> Node
			return connect(el2, el1);
		} else if (el1.isRequisite() && el2.isEntity()) {
			// Requisite -> Entity
			return connect(el2, el1);
		}
		if (result) {
			changed.add(el1);
			changed.add(el2);
			fireContextElementsChanged(changed);
		}
		return result;
	}

	public boolean disconnect(ContextElement el1, ContextElement el2) {
		Set<ContextElement> changed = new HashSet<ContextElement>();
		boolean result = false;
		if (el1.isEntity() && el2.isNode()) {
			// Entity -> Requisite
			// Entity -> Entity
			((Entity) el1).removeChildPr((Node) el2);
			((Node) el2).removeParentPr((Entity) el1);
			result = true;
		} else if (el1.isNode() && (el2.isMM() || el2.isKey())) {
			if (el2.isMM()) {
				// Node -> MM
				((MM) el2).removeMMElementPr((Node) el1);
				((Node) el1).removeMMPr((MM) el2);
				result = true;
			} else if (el2.isKey()) {
				// Node -> Key
				if (((Key) el2).getEntity() == el1) {
					result = false;
				} else {
					// el2 - key; el1 - node
					((Key) el2).getEntity().removeChildPr((Node) el1);
					((Key) el2).removeKeyElementPr((Node) el1);
					((Node) el1).removeKeyInPr((Key) el2);
					changed.add(((Key) el2).getEntity());
					result = true;
				}
			}
		} else if (el2.isNode() && (el1.isMM() || el1.isKey())) {
			// MM -> Node
			// Key -> Node
			return disconnect(el2, el1);
		} else if (el1.isRequisite() && el2.isEntity()) {
			// Requisite -> Entity
			return disconnect(el2, el1);
		}
		if (result) {
			changed.add(el1);
			changed.add(el2);
			fireContextElementsChanged(changed);
		}
		return result;
	}

	//
	// elements extractors
	//

	public Entity getEntityByEId(int eId) {
		for (Iterator<Entity> it1 = entities.iterator(); it1.hasNext();) {
			Entity cur = it1.next();
			if (cur.getId() == eId) {
				return cur;
			}
		}
		return null;
	}

	public Requisite getRequisiteByRId(int rId) {
		for (Requisite req : requisites) {
			if (req.getId() == rId) {
				return req;
			}
		}
		return null;
	}

	public Key getKeyByKId(int kId) {
		for (Key cur : keys) {
			if (cur.getId() == kId) {
				return cur;
			}
		}
		return null;
	}

	public MM getMMByMId(int mId) {
		for (Iterator<MM> it1 = mMs.iterator(); it1.hasNext();) {
			MM cur = it1.next();
			if (cur.getId() == mId) {
				return cur;
			}
		}
		return null;
	}

	//
	// others
	//

	public Set<ContextElement> getAllElements() {
		Set<ContextElement> set = new HashSet<ContextElement>();
		set.addAll(entities);
		set.addAll(requisites);
		set.addAll(keys);
		set.addAll(mMs);
		return set;
	}

	public Set<Node> getNodes() {
		Set<Node> nodes = new HashSet<Node>(entities);
		nodes.addAll(requisites);
		return nodes;
	}

	public Set<Entity> getEntities() {
		return new HashSet<Entity>(entities);
	}

	public Set<Requisite> getRequisites() {
		return new HashSet<Requisite>(requisites);
	}

	public Set<Key> getKeys() {
		return new HashSet<Key>(keys);
	}

	public Set<MM> getMMs() {
		return new HashSet<MM>(mMs);
	}

	public void clean() {
		System.out.println("Cleaning...");
		Set<ContextElement> allElems = new HashSet<ContextElement>();
		allElems.addAll(entities);
		allElems.addAll(requisites);
		allElems.addAll(keys);
		allElems.addAll(mMs);
		entities.clear();
		requisites.clear();
		keys.clear();
		mMs.clear();
		if (!allElems.isEmpty())
			fireContextElementsRemoved(allElems);
	}

	/**
	 * public void addEntity(Entity entity) throws UsedEntityIdException{ if
	 * (getEntityByEId(entity.getEId()) != null){ entities.add(entity); }else{
	 * throw new UsedEntityIdException(); } } public void addRequisite(Requisite
	 * requisite) throws UsedRequisiteIdException{ if
	 * (getRequisiteByRId(requisite.getRId()) != null){
	 * requisites.add(requisite); }else{ throw new UsedRequisiteIdException(); }
	 * } public void addKey(Key key) throws UsedKeyIdException{ if
	 * (getKeyByKId(key.getKId()) != null){ keys.add(key); }else{ throw new
	 * UsedKeyIdException(); } } public void addMM(MM mM) throws
	 * UsedMMIdException{ if (getMMByMId(mM.getMId()) != null){ mMs.add(mM);
	 * }else{ throw new UsedMMIdException(); } }
	 * 
	 * @param file
	 * @exception Exception
	 */
	public void buildFromXmlFile(File file)
			throws ParserConfigurationException, SAXException, IOException {
		buildFromXmlIStream(new FileInputStream(file));
	}

	public void buildFromXmlIStream(InputStream xmlIS)
			throws ParserConfigurationException, SAXException, IOException {

		System.out.println("Parsing XML-doc...");

		this.clean();

		// parse an XML document into a DOM tree
		DocumentBuilder parser = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = parser.parse(xmlIS);

		// create a SchemaFactory capable of understanding WXS schemas
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// load a WXS schema, represented by a Schema instance
		java.net.URL xsdURL = Context.class.getResource(BDDESCR_RES_STR);

		// getting file content
		String xsdContent = Utils.convertIStreamToString(xsdURL.openStream());

		StringBuilder editableContent = new StringBuilder(xsdContent);

		/*
		 * for (Object key : xmlTagNames.keySet()){ xsdContent =
		 * xsdContent.replaceAll("\'" + (String)key + "\'", "\'" +
		 * xmlTagNames.getProperty((String)key) + "\'"); }
		 */

		// Modifying schema according to the names in XMLProps
		for (Object key : xmlTagNames.keySet()) {
			String st = (String) key;
			int ind = 0;
			while ((ind = editableContent.indexOf(st, ind)) != -1) {
				// System.out.println(ind);
				String newName = xmlTagNames.getProperty(st);
				editableContent.replace(ind, ind + st.length(), newName);
				ind += newName.length();
			}
		}
		xsdContent = editableContent.toString();

		// setting the schema
		InputStream is = new ByteArrayInputStream(xsdContent.getBytes("UTF-8"));
		Source schemaFile = new StreamSource(is);
		Schema schema = factory.newSchema(schemaFile);

		// create a Validator instance, which can be used to validate an
		// instance document
		Validator validator = schema.newValidator();

		// validate the DOM tree
		validator.validate(new DOMSource(doc));

		// we'll use this list for firing the newElementsEvent
		List<ContextElement> newElements = new LinkedList<ContextElement>();

		{// Обработка понятий
			NodeList entities = doc.getElementsByTagName(xmlTagNames
					.getProperty("entity"));
			for (int i = 0; i < entities.getLength(); i++) {
				// получим идентификатор
				int id = Integer.parseInt(((Element) entities.item(i))
						.getAttribute(xmlTagNames.getProperty("enId")));
				// получим name
				String name = ((Element) entities.item(i)).getElementsByTagName(
						xmlTagNames.getProperty("name")).item(0)
						.getTextContent();
				// получим семантику
				NodeList semList = ((Element) entities.item(i))
						.getElementsByTagName(xmlTagNames.getProperty("description"));
				String sem = "";
				if (semList.getLength() > 0) {
					sem = semList.item(0).getTextContent();
				}
				/*
				 * System.out.println("пон." + "#" + id + "\t" + name + "\t" + sem
				 * );
				 */
	
				Entity ent = createEntityPr(id, name, sem);
				if (ent == null)
					System.out.println("Enitity wasn't imported: " + "#" + id
							+ "\t" + name + "\t" + sem);
				else
					newElements.add(ent);
	
			}
		}

		// System.out.println("----");
		{// Обработка requisiteов
			NodeList requisites = doc.getElementsByTagName(xmlTagNames
					.getProperty("requisite"));
			for (int i = 0; i < requisites.getLength(); i++) {
				// получим идентификатор
				int id = Integer.parseInt(((Element) requisites.item(i))
						.getAttribute(xmlTagNames.getProperty("reqId")));
				// получим name
				String name = ((Element) requisites.item(i)).getElementsByTagName(
						xmlTagNames.getProperty("name")).item(0)
						.getTextContent();
				// получим семантику
				NodeList semList = ((Element) requisites.item(i))
						.getElementsByTagName(xmlTagNames.getProperty("description"));
				String sem = "";
				if (semList.getLength() > 0) {
					sem = semList.item(0).getTextContent();
				}
				// получим обязательность
				boolean required = false;
				if (((Element) requisites.item(i)).getElementsByTagName(
						xmlTagNames.getProperty("required")).getLength() > 0) {
					required = true;
				}
	
				/*
				 * System.out.println("рекв" + "#" + id + "\t" + name + "\t" +
				 * nullability );
				 */
	
				Requisite requisite = createRequisitePr(id, name, sem, required);
				if (requisite == null)
					System.out.println("Requisite wasn't imported: " + "#" + id
							+ "\t" + name + "\t" + sem + "\t" + required);
				else
					newElements.add(requisite);
	
			}
		}
		// System.out.println("----");
		{// Обработка вложенностей
			NodeList nestings = doc.getElementsByTagName(xmlTagNames
					.getProperty("inclusion"));
			for (int i = 0; i < nestings.getLength(); i++) {
				// получим идентификатор понятия
				int id = Integer.parseInt(((Element) nestings.item(i))
						.getAttribute(xmlTagNames.getProperty("enId")));
	
				// System.out.print("влож.д.пон#" + id);
	
				Entity entity = this.getEntityByEId(id);
				if (entity != null) {
					// получим список requisiteов
					NodeList requisitesNodes = ((Element) nestings.item(i))
							.getElementsByTagName(xmlTagNames
									.getProperty("requisiteLink"));
					for (int j = 0; j < requisitesNodes.getLength(); j++) {
						// System.out.print("\tрек#" +
						// ((Element)requisitesNodes.item(j)).getAttribute("reqId"));
						int reqId = Integer.parseInt(((Element) requisitesNodes
								.item(j)).getAttribute(xmlTagNames
								.getProperty("reqId")));
						Requisite req = getRequisiteByRId(reqId);
						if (req == null)
							System.out
									.println("Requisite #" + reqId + " not found");
						else {
							entity.addChildPr(req);
							req.addParentPr(entity);
						}
					}
					// получим список понятий
					NodeList entitiesNodes = ((Element) nestings.item(i))
							.getElementsByTagName(xmlTagNames
									.getProperty("entityLink"));
					for (int j = 0; j < entitiesNodes.getLength(); j++) {
						// System.out.print("\tпон#" +
						// ((Element)entitiesNodes.item(j)).getAttribute("enId"));
						Entity ent = this.getEntityByEId(Integer
								.parseInt(((Element) entitiesNodes.item(j))
										.getAttribute(xmlTagNames
												.getProperty("enId"))));
						entity.addChildPr(ent);
						ent.addParentPr(entity);
					}
					// System.out.println();
				} else {
					System.out.println("Entity " + id + " not found");
				}
			}
		}

		// System.out.println("----");
		{// Обработка keyей
			NodeList keys = doc.getElementsByTagName(xmlTagNames
					.getProperty("key"));
			for (int i = 0; i < keys.getLength(); i++) {
				// получим идентификатор
				int kId = Integer.parseInt(((Element) keys.item(i))
						.getAttribute(xmlTagNames.getProperty("keyId")));
				/*
				 * System.out.print("key" + "#" + kId );
				 */
				int pId = Integer.parseInt(((Element) keys.item(i))
						.getAttribute(xmlTagNames.getProperty("enId")));
				/*
				 * System.out.print("\tпон." + "#" + pId );
				 */
	
				Key key = createKeyPr(kId, getEntityByEId(pId));
				// disable group manager cause we will have done his job now
				key.setGrMgrEnabled(false);
				if (key != null) {
					
					{ // получим список keyевых requisiteов
						NodeList keyRequisitesNodes = ((Element) keys.item(i))
								.getElementsByTagName(xmlTagNames
										.getProperty("requisiteLink"));
						for (int j = 0; j < keyRequisitesNodes.getLength(); j++) {
							// System.out.print("\tрек#" +
							// ((Element)keyRequisitesNodes.item(j)).getAttribute("reqId"));
							int reqId = Integer.parseInt(((Element) keyRequisitesNodes
									.item(j)).getAttribute(xmlTagNames
									.getProperty("reqId")));
							Requisite req = getRequisiteByRId(reqId);
							if (req != null) {
								key.addKeyElementPr(req);
								req.addKeyInPr(key);
							} else {
								System.out.println("Requisite wasn't found: #" + reqId);
							}
						}
					}
	
					{ // получим список keyевых понятий
						NodeList keyEntitiesNodes = ((Element) keys.item(i))
								.getElementsByTagName(xmlTagNames.getProperty("entityLink"));
						for (int j = 0; j < keyEntitiesNodes.getLength(); j++) {
							// System.out.print("\tпон#" +
							// ((Element)keyEntitiesNodes.item(j)).getAttribute("enId"));
							int entId = Integer.parseInt(((Element) keyEntitiesNodes
									.item(j)).getAttribute(xmlTagNames
									.getProperty("enId")));
							Entity ent = getEntityByEId(entId);
							if (ent != null) {
								key.addKeyElementPr(ent);
								ent.addKeyInPr(key);
							} else {
								System.out.println("Entity wasn't found: #" + entId);
							}
						}
						newElements.add(key);
						// System.out.println();
					}
					
					{// Создадим группы
						NodeList keyGroupNodes = ((Element) keys.item(i)).getElementsByTagName(xmlTagNames.getProperty("group"));
						for (int j = 0; j < keyGroupNodes.getLength(); j++) {
							Element el = (Element) keyGroupNodes.item(j);
							// num
							int num = Integer.parseInt(el.getAttribute(xmlTagNames.getProperty("num")));
							// requisite
							int rid = Integer.parseInt(el.getAttribute(xmlTagNames.getProperty("reqId")));
							// name
							String name = null;
							NodeList nameSingle = el.getElementsByTagName(xmlTagNames.getProperty("name")); 
							if (nameSingle.getLength() == 1)
								name = ((Element) nameSingle.item(0)).getTextContent();
							// default
							boolean def = Boolean.parseBoolean(el.getAttribute(xmlTagNames.getProperty("default")));
							Requisite req = getRequisiteByRId(rid);
							if (req != null) {
								Group g = new Group(req, key);
								if (name != null)
									g.setName(name);
								if (num != key.grMgr.addGroupPr(g, def)) {
									System.out.println("There is an error with group numbers of " + key);								
								}
							} else {
								System.out.println("Entity wasn't found: #" + rid);
							}
						}
					}				
				} else {
					System.out.println("Key wasn't imported: #" + kId
							+ " for Entity #" + pId);
				}
			}
		}
		
		{// Обработка вложенностей групп
			NodeList nestings = doc.getElementsByTagName(xmlTagNames.getProperty("groupInclusion"));
			for (int i = 0; i < nestings.getLength(); i++) {
				Element el = (Element) nestings.item(i);
				// получим идентификатор keyа
				int id = Integer.parseInt(el.getAttribute(xmlTagNames.getProperty("keyId")));
				// num группы
				int num = Integer.parseInt(el.getAttribute(xmlTagNames.getProperty("num")));
				
				Key key = getKeyByKId(id);
				if (key != null) {
					Group g = key.grMgr.getGroups().get(num);
					// получим список вложенных групп
					NodeList ins = el.getElementsByTagName(xmlTagNames.getProperty("groupLink"));
					for (int j = 0; j < ins.getLength(); j++) {
						Element inEl = (Element) ins.item(j);
						int inKid = Integer.parseInt(inEl.getAttribute(xmlTagNames.getProperty("keyId")));
						int inNum = Integer.parseInt(inEl.getAttribute(xmlTagNames.getProperty("num")));
						Key inKey = getKeyByKId(inKid);
						if (inKey != null) {
							g.addInGroup(inKey.grMgr.getGroups().get(inNum));							
						} else {
							System.out.println("Key #" + inKey + " not found");
						}
					}
					// получим requisite
					NodeList reqSingle = el.getElementsByTagName(xmlTagNames.getProperty("requisiteLink"));
					if (reqSingle.getLength() == 1) {
						Element reqEl = (Element) reqSingle.item(0);
						int reqId = Integer.parseInt(reqEl.getAttribute(xmlTagNames.getProperty("reqId")));
						Requisite req = getRequisiteByRId(reqId);
						if (req != null) {
							g.setReq(req);							
						} else {
							System.out.println("Requisite #" + reqId + " not found");
						}
					}
				} else {
					System.out.println("Key " + id + " not found");
				}
			}
		}
		

		// System.out.println("----");
		{// Обработка m_m
			NodeList mMs = doc.getElementsByTagName(xmlTagNames.getProperty("m_m"));
			for (int i = 0; i < mMs.getLength(); i++) {
				// получим идентификатор
				int id = Integer.parseInt(((Element) mMs.item(i))
						.getAttribute(xmlTagNames.getProperty("mId")));
				/*
				 * System.out.print("m_m." + "#" + id );
				 */
	
				MM mM = createMMPr(id);
				if (mM != null) {
					// получим список связанных понятий
					NodeList mMEntitiesNodes = ((Element) mMs.item(i))
							.getElementsByTagName(xmlTagNames
									.getProperty("entityLink"));
					for (int j = 0; j < mMEntitiesNodes.getLength(); j++) {
						// System.out.print("\tпон.#" +
						// ((Element)mMEntitiesNodes.item(j)).getAttribute("enId"));
						int entId = Integer.parseInt(((Element) mMEntitiesNodes
								.item(j)).getAttribute(xmlTagNames
								.getProperty("enId")));
						Entity ent = getEntityByEId(entId);
						if (ent != null) {
							mM.addMMElementPr(ent);
							ent.addMMPr(mM);
						} else {
							System.out.println("Entity wasn't found: #" + entId);
						}
					}
					// получим список связанных requisiteов
					NodeList mMRequisitesNodes = ((Element) mMs.item(i))
							.getElementsByTagName(xmlTagNames
									.getProperty("requisiteLink"));
					for (int j = 0; j < mMRequisitesNodes.getLength(); j++) {
						// System.out.print("\tпон.#" +
						// ((Element)mMRequisitesNodes.item(j)).getAttribute("reqId"));
						int reqId = Integer.parseInt(((Element) mMRequisitesNodes
								.item(j)).getAttribute(xmlTagNames
								.getProperty("reqId")));
						Requisite req = getRequisiteByRId(reqId);
						if (req != null) {
							mM.addMMElementPr(req);
							req.addMMPr(mM);
						} else {
							System.out.println("Requisite wasn't found: #" + reqId);
						}
					}
					newElements.add(mM);
				} else {
					System.out.println("MM wasn't imported: #" + id);
				}
				// System.out.println();
			}
		}

		for (ContextElement el : newElements) {
			if (el.isKey()) {
				((Key) el).setGrMgrEnabled(true);				
			}				
		}		 
		 
		fireContextElementsAdded(newElements);
	}

	public void saveToXmlFile(File file) throws Exception {
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();

		FileOutputStream os = new FileOutputStream(file);
		saveToXmlOStream(os);
		os.close();
	}

	public void saveToXmlOStream(OutputStream os)
			throws ParserConfigurationException, TransformerException,
			IOException {
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		DocumentBuilder p = f.newDocumentBuilder();
		Document doc = p.newDocument();

		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		StreamResult result = new StreamResult(os);
		DOMSource source = new DOMSource(doc);

		Element entityTreeEl = doc.createElement(xmlTagNames
				.getProperty("entityTree"));
		doc.appendChild(entityTreeEl);

		// создали узел, сразу связали его с родителями
		// потом добавляем атрибуты и детей

		// добавляем понятия
		for (Iterator<Entity> it1 = entities.iterator(); it1.hasNext();) {
			Entity cur = it1.next();
			Element entityEl = doc.createElement(xmlTagNames
					.getProperty("entity"));
			entityTreeEl.appendChild(entityEl);
			entityEl.setAttribute(xmlTagNames.getProperty("enId"), Integer
					.toString(cur.getId()));
			Element nameEl = doc.createElement(xmlTagNames
					.getProperty("name"));
			entityEl.appendChild(nameEl);
			nameEl.appendChild(doc.createTextNode(cur.getName()));
			String semantics = cur.getSemantics().trim();
			if (!semantics.isEmpty()) {
				Element semanticsEl = doc.createElement(xmlTagNames
						.getProperty("description"));
				entityEl.appendChild(semanticsEl);
				semanticsEl.appendChild(doc.createTextNode(semantics));
			}
		}

		// добавляем requisiteы
		for (Iterator<Requisite> it1 = requisites.iterator(); it1.hasNext();) {
			Requisite cur = it1.next();
			Element requisiteEl = doc.createElement(xmlTagNames
					.getProperty("requisite"));
			entityTreeEl.appendChild(requisiteEl);
			requisiteEl.setAttribute(xmlTagNames.getProperty("reqId"), Integer
					.toString(cur.getId()));
			Element nameEl = doc.createElement(xmlTagNames
					.getProperty("name"));
			requisiteEl.appendChild(nameEl);
			nameEl.appendChild(doc.createTextNode(cur.getName()));
			String semantics = cur.getSemantics().trim();
			if (!semantics.isEmpty()) {
				Element semanticsEl = doc.createElement(xmlTagNames
						.getProperty("description"));
				requisiteEl.appendChild(semanticsEl);
				semanticsEl.appendChild(doc.createTextNode(semantics));
			}
			if (cur.getRequired()) {
				Element required = doc.createElement(xmlTagNames
						.getProperty("required"));
				requisiteEl.appendChild(required);
			}
		}

		// добавляем вложенности
		for (Iterator<Entity> it1 = entities.iterator(); it1.hasNext();) {
			Entity cur = it1.next();
			Set<Node> children = cur.getChildren();
			if (!children.isEmpty()) {
				Element nestingEl = doc.createElement(xmlTagNames
						.getProperty("inclusion"));
				entityTreeEl.appendChild(nestingEl);
				nestingEl.setAttribute(xmlTagNames.getProperty("enId"), Integer
						.toString(cur.getId()));
				for (Iterator<Node> it2 = children.iterator(); it2.hasNext();) {
					Node child = it2.next();
					if (child.isEntity()) {
						Element childEl = doc.createElement(xmlTagNames
								.getProperty("entityLink"));
						nestingEl.appendChild(childEl);
						childEl.setAttribute(xmlTagNames.getProperty("enId"),
								Integer.toString(child.getId()));
					} else if (child.isRequisite()) {
						Element childEl = doc.createElement(xmlTagNames
								.getProperty("requisiteLink"));
						nestingEl.appendChild(childEl);
						childEl.setAttribute(xmlTagNames.getProperty("reqId"),
								Integer.toString(child.getId()));
					}
				}
			}
		}

		// добавляем keyи
		for (Key cur : keys) {
			Element keyEl = doc.createElement(xmlTagNames.getProperty("key"));
			entityTreeEl.appendChild(keyEl);
			keyEl.setAttribute(xmlTagNames.getProperty("keyId"), Integer.toString(cur.getId()));
			keyEl.setAttribute(xmlTagNames.getProperty("enId"), Integer.toString(cur.getEntity().getId()));
			for (Node keyChild : cur.getKeyElements()) {
				if (keyChild.isEntity()) {
					Element keyChildEl = doc.createElement(xmlTagNames.getProperty("entityLink"));
					keyEl.appendChild(keyChildEl);
					keyChildEl.setAttribute(xmlTagNames.getProperty("enId"),
							Integer.toString(keyChild.getId()));
				} else if (keyChild.isRequisite()) {
					Element keyChildEl = doc.createElement(xmlTagNames.getProperty("requisiteLink"));
					keyEl.appendChild(keyChildEl);
					keyChildEl.setAttribute(xmlTagNames.getProperty("reqId"),
							Integer.toString(keyChild.getId()));
				}
			}
			// добавляем группы
			for (Group g : cur.grMgr.getGroups()) {
				Element group = doc.createElement(xmlTagNames.getProperty("group"));
				keyEl.appendChild(group);
				group.setAttribute(xmlTagNames.getProperty("num"),Integer.toString(cur.grMgr.getGroupNum(g)));
				group.setAttribute(xmlTagNames.getProperty("reqId"),Integer.toString(g.getMainReq().getId()));
				if (cur.grMgr.isDefault(g))
					group.setAttribute(xmlTagNames.getProperty("default"), new Boolean(true).toString());
				Element nameEl = doc.createElement(xmlTagNames.getProperty("name"));
				group.appendChild(nameEl);
				nameEl.appendChild(doc.createTextNode(g.getName()));
			}
		}
		
		// добавляем inclusion групп
		for (Key cur : keys) {
			for (Group g : cur.grMgr.getGroups()) {
				Element grNestEl = doc.createElement(xmlTagNames.getProperty("groupInclusion"));
				entityTreeEl.appendChild(grNestEl);
				grNestEl.setAttribute(xmlTagNames.getProperty("keyId"), Integer.toString(cur.getId()));
				grNestEl.setAttribute(xmlTagNames.getProperty("num"), Integer.toString(cur.grMgr.getGroupNum(g)));
				for (Group in : g.getInGroups()) {
					Element inEl = doc.createElement(xmlTagNames.getProperty("groupLink"));
					grNestEl.appendChild(inEl);
					inEl.setAttribute(xmlTagNames.getProperty("keyId"),Integer.toString(in.getOwner().getId()));
					inEl.setAttribute(xmlTagNames.getProperty("num"),Integer.toString(in.getOwner().grMgr.getGroupNum(in)));
				}
				if (g.getReq() != null) {
					Element reqEl = doc.createElement(xmlTagNames.getProperty("requisiteLink"));
					grNestEl.appendChild(reqEl);
					reqEl.setAttribute(xmlTagNames.getProperty("reqId"), Integer.toString(g.getReq().getId()));					
				}
			}
		}

		// добавляем ММсы
		for (Iterator<MM> it1 = mMs.iterator(); it1.hasNext();) {
			MM cur = it1.next();
			Element mMEl = doc.createElement(xmlTagNames.getProperty("m_m"));
			entityTreeEl.appendChild(mMEl);
			mMEl.setAttribute(xmlTagNames.getProperty("mId"), Integer
					.toString(cur.getId()));
			Set<Node> mMParticipants = cur.getMMElements();
			for (Iterator<Node> it2 = mMParticipants.iterator(); it2.hasNext();) {
				Node mMParticipant = it2.next();
				if (mMParticipant.isEntity()) {
					Element mMParticipantEl = doc.createElement(xmlTagNames
							.getProperty("entityLink"));
					mMEl.appendChild(mMParticipantEl);
					mMParticipantEl.setAttribute(
							xmlTagNames.getProperty("enId"), Integer
									.toString(mMParticipant.getId()));
				} else if (mMParticipant.isRequisite()) {
					Element mMParticipantEl = doc.createElement(xmlTagNames
							.getProperty("requisiteLink"));
					mMEl.appendChild(mMParticipantEl);
					mMParticipantEl.setAttribute(
							xmlTagNames.getProperty("reqId"), Integer
									.toString(mMParticipant.getId()));
				}
			}
		}

		// transform source into result will do save
		transformer.transform(source, result);

		os.flush();

	}

	public void loadXmlTagNames(InputStream is) throws IOException,
			InvalidPropertiesFormatException {
		xmlTagNames.loadFromXML(is);
	}

	public void saveXmlTagNames(OutputStream os) throws IOException {
		xmlTagNames.storeToXML(os, null);
	}

	protected Properties getXmlTagNames() {
		return (Properties) xmlTagNames.clone();
	}

	protected void setXmlTagNames(Properties xmlTagNames) {
		this.xmlTagNames = xmlTagNames;
	}

	protected int getFreeId(TYPE type) {
		Set<Integer> ids = new HashSet<Integer>();
		if (type.equals(TYPE.REQUISITE)) {
			for (Requisite req : requisites) {
				ids.add(req.getId());
			}
		} else if (type.equals(TYPE.ENTITY)) {
			for (Entity ent : entities) {
				ids.add(ent.getId());
			}
		} else if (type.equals(TYPE.KEY)) {
			for (Key key : keys) {
				ids.add(key.getId());
			}
		} else if (type.equals(TYPE.MM)) {
			for (MM mM : mMs) {
				ids.add(mM.getId());
			}
		}
		for (Integer i = 1; i < Integer.MAX_VALUE; i++) {
			if (!ids.contains(i)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * public ContextElement create(TYPE type, Object obj) { if
	 * (type.equals(Context. TYPE.ENTITY)) { return new Entity(this,
	 * "РќРѕРІРѕРµ РїРѕРЅСЏС‚РёРµ"); } else if (type.
	 * equals(Context.TYPE.REQUISITE)) { return new Requisite(this,
	 * "РќРѕРІС‹Р№ СЂРµРєРІРёР·РёС‚"); } else if (type.equals(Context.TYPE.KEY))
	 * { return new Key(this, ); } else if (type.equals(Context.TYPE.MM)) { }
	 * return null; }
	 * 
	 * @param type
	 */
	protected String getDefNewName(TYPE type) {
		switch (type) {
		case ENTITY:
			return "Untitled entity";
		case REQUISITE:
			return "Untitled requisite";
		}
		return null;
	}

	protected String getUniqueName(TYPE type, String name) {
		Set<String> names = new HashSet<String>();
		if (type.equals(TYPE.REQUISITE)) {
			for (Requisite req : requisites) {
				names.add(req.getName());
			}
		} else if (type.equals(TYPE.ENTITY)) {
			for (Entity ent : entities) {
				names.add(ent.getName());
			}
		}
		if (names.contains(name))
			return getUniqueName_(names, name, 1);
		return name;
	}

	protected String getUniqueName_(Set<String> names, String name, int counter) {
		if (names.contains(name + " (" + counter + ")"))
			return getUniqueName_(names, name, ++counter);
		return name + " (" + counter + ")";
	}

	protected boolean isMy(ContextElement el) {
		if (el == null)
			return false;
		if (el.getCtx() != this)
			return false;
		switch (el.type) {
		case ENTITY:
			return entities.contains(el);
		case REQUISITE:
			return requisites.contains(el);
		case KEY:
			return keys.contains(el);
		case MM:
			return mMs.contains(el);
		default:
			assert false;
		}
		return false;
	}

	public void manageTagNames(JFrame main) {
		TagsEditor ed = new TagsEditor(main, this);
		ed.setLocationRelativeTo(main);
		ed.setVisible(true);
	}

	//
	// Observer pattern
	//

	public void addContextListener(ContextListener l) {
		listenerList.add(ContextListener.class, l);
	}

	public void removeContextListener(ContextListener l) {
		listenerList.remove(ContextListener.class, l);
	}

	// Notify all listeners that have registered interest for
	// notification on this event type. The event instance
	// is lazily created using the parameters passed into
	// the fire method.
	protected void fireContextElementsAdded(
			Collection<ContextElement> addedElements) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ContextListener.class) {
				// Create the event:
				ContextEvent contextEvent = new ContextEvent(this);
				contextEvent.setNewElements(addedElements);
				((ContextListener) listeners[i + 1])
						.elementsAdded(contextEvent);
			}
		}
		System.out.println("Elements added: " + addedElements);
	}

	protected void fireContextElementsRemoved(
			Collection<ContextElement> removedElements) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ContextListener.class) {
				// Create the event:
				ContextEvent contextEvent = new ContextEvent(this);
				contextEvent.setRemovedElements(removedElements);
				((ContextListener) listeners[i + 1])
						.elementsRemoved(contextEvent);
			}
		}
		System.out.println("Elements removed: " + removedElements);
	}

	protected void fireContextElementsChanged(
			Collection<ContextElement> changedElements) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ContextListener.class) {
				// Create the event:
				ContextEvent contextEvent = new ContextEvent(this);
				contextEvent.setChangedElements(changedElements);
				((ContextListener) listeners[i + 1])
						.elementsChanged(contextEvent);
			}
		}
		System.out.println("Elements changed: " + changedElements);
	}

	private void fireContextElementChanged(ContextElement el) {
		Set<ContextElement> elementsChanged = new HashSet<ContextElement>();
		elementsChanged.add(el);
		fireContextElementsChanged(elementsChanged);
	}

	private void fireContextElementAdded(ContextElement el) {
		Set<ContextElement> elementsAdded = new HashSet<ContextElement>();
		elementsAdded.add(el);
		fireContextElementsAdded(elementsAdded);
	}
/*
	private void fireContextElementRemoved(ContextElement el) {
		Set<ContextElement> elementsRemoved = new HashSet<ContextElement>();
		elementsRemoved.add(el);
		fireContextElementsRemoved(elementsRemoved);
	}
*/
	private void fireContextElementConverted(ContextElement elOld,
			ContextElement elNew) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ContextListener.class) {
				// Create the event:
				ContextEvent contextEvent = new ContextEvent(this);
				contextEvent.setConvertedElementOld(elOld);
				contextEvent.setConvertedElementNew(elNew);
				((ContextListener) listeners[i + 1])
						.elementConverted(contextEvent);
			}
		}
		System.out.println("Element converted: " + elOld + " -> " + elNew);
	}

	//
	// commented
	//

	/*
	 * protected void add(ContextElement element) { addPr(element);
	 * fireContextElementAdded(element);
	 * 
	 * if (element.isKey()) {
	 * fireContextElementChanged(((Key)element).getEntity()); } }
	 * 
	 * protected void addPr(ContextElement element) { if (element.isEntity()) {
	 * entities.add((Entity)element); } else if (element.isRequisite()) {
	 * requisites.add((Requisite)element); } else if (element.isKey()) {
	 * keys.add((Key)element); } else if (element.isMM()) {
	 * mMs.add((MM)element); } }
	 */

}
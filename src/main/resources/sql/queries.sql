LOGIN=SELECT * FROM users WHERE username=? AND password=?
ALL_PARTS=SELECT id, name, maker, description, price, quantity,catid FROM parts
PART_BY_CRITERIA=SELECT id, name, maker, description, price, quantity,catid FROM parts WHERE
PART_BY_ID=SELECT id, name, maker, description, price, quantity,catid FROM parts WHERE id=?
ADD_PART=INSERT INTO parts(name, maker, description, price, quantity, catid) VALUES(?, ?, ?, ?, ?,?)
PARTS_QUANTITY_DESC=SELECT id,name, maker, description, price, quantity,catid FROM parts ORDER BY quantity DESC
PARTS_PRICE_DESC=SELECT id,name, maker, description, price, quantity,catid FROM parts ORDER BY price DESC
UPDATE_PART=UPDATE parts SET name=?, maker=?, description=?, price=?, quantity=?, catid=? WHERE id=?
DELETE_PART=DELETE FROM parts WHERE id=?
ALL_CATEGORIES=SELECT id, name, description,image FROM categories
ADD_CATEGORY=INSERT INTO categories( name, description, image) VALUES(?, ?, ?)
DELETE_CATEGORY=DELETE FROM categories WHERE id=?
GET_CATEGORY_BY_ID=SELECT id, name,description,image FROM categories WHERE id=?
ALL_BILLS=SELECT id, clientname, clientphone, totalprice, date FROM bills
ADD_BILL=INSERT INTO bills(totalprice, clientname, clientphone, date) VALUES(?, ?, ?, ?)
UPDATE_BILL=UPDATE bills SET clientname=?, clientphone=?, totalprice=? WHERE id=?
UPDATE_COMMAND=UPDATE commands SET quantity=?, priceconsidered=? WHERE partid=? AND billid=?
DELETE_BILL=DELETE FROM bills WHERE id=?
ADD_PART_TO_CHART=INSERT INTO commands(partid, billid, quantity, priceconsidered) VALUES(?, ?, ?, ?)
PART_IN_BILL=SELECT parts.id, parts.name, parts.maker, parts.description, commands.priceconsidered, commands.quantity, parts.catid FROM bills, commands, parts WHERE bills.id = commands.billid AND parts.id = commands.partid AND bills.id=?
LAST_INSERTED=SELECT last_insert_rowid()

// src/pages/DashboardPage.js
import React, { useState, useEffect } from 'react';
import { FaRupeeSign, FaBoxes, FaBan } from 'react-icons/fa';
import Modal from '../components/Modal';
import './DashboardPage.css';
import { useNavigate } from 'react-router-dom';
import { useConfig } from "./ConfigProvider";


const DashboardPage = () => {
  const [dashboardData, setDashboardData] = useState({});
  const [sales, setSales] = useState([]);
  const [timeRange, setTimeRange] = useState('today');
  const [isNewCusModalOpen, setIsNewCusModalOpen] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [prodName, seProdName] = useState("");
      const [category, setCategory] = useState("");
      const [price, setPrice] = useState("");
      const [stock, setStock] = useState("");
      const [tax, setTax] = useState("");
      const [isAddProdModalOpen, setIsAddProdModalOpen] = useState(false);

const config = useConfig();
var apiUrl="";
const navigate = useNavigate();
  if(config){
  console.log(config.API_URL);
  apiUrl=config.API_URL;
  }

  useEffect(() => {
    fetch(`${apiUrl}/api/shop/get/dashboardDetails/${timeRange}`)
      .then(res => {
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
        return res.json();
      })
      .then(data => setDashboardData(data))
      .catch(err => {
        console.error("Error fetching dashboardData:", err);
        alert("Something went wrong while fetching dashboard details.");
      });
  }, [timeRange]);

  useEffect(() => {
    fetch(apiUrl+"/api/shop/get/sales")
      .then(res => {
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
        return res.json();
      })
      .then(data => setSales(data))
      .catch(err => {
        console.error("Error fetching sales:", err);
        alert("Something went wrong while fetching sales data.");
      });
  }, []);

  const recentSales = sales.slice(0, 3);

  const handleAddCustomer = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('jwt_token');
    try {
      const payload = { name, email, phone };
    //  alert(payload);
    console.log(payload);
      const response = await fetch(apiUrl+"/api/shop/create/customer", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      alert("Customer added successfully!");
    } catch (error) {
      console.error("Error adding customer:", error);
      alert("Something went wrong while adding the customer.");
    }
    setIsNewCusModalOpen(false);
    setName("");
    setEmail("");
    setPhone("");
  };



  const handleAddProduct = async (e) => {

          e.preventDefault();
          // API CALL: Add new product
          // POST /api/products
          // Payload: { name, category, price, stock }
          // Response: { success: true, product: { ... } }

          try {
                   const payload = {name, category, price, stock, tax };
                      console.log("new product Payload:", payload);
                      const response = await fetch(apiUrl+"/api/shop/create/product", {
                        method: "POST",
                        headers: {
                          "Content-Type": "application/json",
                        },
                        body: JSON.stringify(payload),

                      });
                      //console.log(body);


                      const data = await response.json();
                      console.log("API response:", data);
                      if (!response.ok) {
                                    throw new Error(`HTTP error! status: ${response.status}`);
                                  }

                      } catch (error) {
                          console.error("Error adding customer:", error);
                          alert("Something went wrong while adding the customer.");
                        }


          alert('New product added! (Demo)');
          setIsAddProdModalOpen(false);
      }


  return (
    <div className="dashboard">
      <h2>Dashboard</h2>

      <div className="time-range-selector glass-card">
        <label htmlFor="timeRange">📅 </label>
        <select
          id="timeRange"
          value={timeRange}
          onChange={(e) => setTimeRange(e.target.value)}
          className="dropdown"
        >
          <option value="today">Today</option>
          <option value="lastWeek">Last Week</option>
          <option value="lastMonth">Last Month</option>
          <option value="lastYear">Last Year</option>
        </select>
      </div>

      <div className="stats-grid">
        <div className="stat-card glass-card">
          <FaRupeeSign className="icon revenue" />
          <div>
            <p>Total Revenue</p>
            <h3>₹{dashboardData.monthlyRevenue?.toLocaleString()}</h3>
          </div>
        </div>
        <div className="stat-card glass-card">
          <FaBoxes className="icon units" />
          <div>
            <p>Total Units Sold</p>
            <h3>{dashboardData.totalUnitsSold}</h3>
          </div>
        </div>
        <div className="stat-card glass-card">
          <FaRupeeSign className="icon tax" />
          <div>
            <p>Tax Collected</p>
            <h3>₹{dashboardData.taxCollected?.toLocaleString()}</h3>
          </div>
        </div>
        <div className="stat-card glass-card">
          <FaBan className="icon stock" />
          <div>
            <p>Out of Stock Products</p>
            <h3>{dashboardData.outOfStockCount}</h3>
          </div>
        </div>
      </div>

      <div className="quick-shortcuts glass-card">
        <h3>Quick Shortcuts</h3>
        <div className="shortcuts-container">
                        <button
                            className="btn"
                            type="button"
                            onClick={() => navigate("/billing")} // 👈 navigate to your page
                          >New Sale</button>
          <button className="btn"
                                              className="btn"
                                              type="button"
                                              onClick={() => setIsAddProdModalOpen(true)}
                                            >Add Product</button>

          <button
            className="btn"
            type="button"
            onClick={() => setIsNewCusModalOpen(true)}
          >
            New Customer
          </button>
               <button
                  className="btn"
                  type="button"
                  onClick={() => navigate("/reports")} // 👈 navigate to your page
                >
                  Generate Report
                </button>
                <button
                                  className="btn"
                                  type="button"
                                  onClick={() => navigate("/analytics")} // 👈 navigate to your page
                                >
                                  Analytics
                                </button>

        </div>
      </div>

      <div className="recent-sales glass-card">
        <h3>Recent Sales</h3>
        <table className="data-table">
          <thead>
            <tr>
              <th>Invoice ID</th>
              <th>Customer</th>
              <th>Date</th>
              <th>Total</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {recentSales.map(sale => (
              <tr key={sale.id}>
                <td>{sale.id}</td>
                <td>{sale.customer}</td>
                <td>{sale.date}</td>
                <td>₹{sale.total.toLocaleString()}</td>
                <td>
                  <span className={sale.status === 'Paid' ? 'status-paid' : 'status-pending'}>
                    {sale.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* New Customer Modal */}
     {isNewCusModalOpen && (
       <Modal show={isNewCusModalOpen} onClose={() => setIsNewCusModalOpen(false)}>
         <h2>Add New Customer</h2>
         <form onSubmit={handleAddCustomer} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
           <div className="form-group">
                       <label>Full Name</label>
                       <input type="text" required value={name} onChange={(e) => setName(e.target.value)} />
                     </div>
                     <div className="form-group">
                       <label>Email</label>
                       <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
                     </div>
                     <div className="form-group">
                       <label>Phone Number</label>
                       <input type="tel" required value={phone} onChange={(e) => setPhone(e.target.value)} />
                     </div>
                     <div className="form-actions">
                       <button type="submit" className="btn">Add Customer</button>
                     </div>
         </form>
       </Modal>
     )}

     <Modal title="Add New Product" show={isAddProdModalOpen} onClose={() => setIsAddProdModalOpen(false)}>
                     <form onSubmit={handleAddProduct}>
                         <div className="form-group">
                             <label>Product Name</label>
                             <input type="text"
                             required
                             value={name}
                             onChange={(e) => setName(e.target.value)}
                             />
                         </div>
                           <div className="form-group">
                                       <label>Category</label>
                                       <select
                                         required
                                         value={category}
                                         onChange={(e) => setCategory(e.target.value)}
                                       >
                                         <option value="">-- Select Category --</option>

                      <option value="Smatphones">Smatphones</option>
                      <option value="Laptops and Computers">Laptops and Computers</option>
                      <option value="Audio">Audio</option>
                      <option value="Videos">Videos</option>
                      <option value="Accessories">Accessories</option>
                       <option value="Others">Others</option>
                                       </select>
                                     </div>
                         <div className="form-group">
                             <label>Price</label>
                             <input type="number"
                             required
                             value={price}
                             onChange={(e) => setPrice(e.target.value)}
                              />
                         </div>
                          <div className="form-group">
                             <label>Stock Quantity</label>
                             <input type="number"
                             required
                             value={stock}
                             onChange={(e) => setStock(e.target.value)}
                             />
                         </div>
                         <div className="form-group">
                             <label>Tax Percent</label>
                             <input type="number"
                             required
                             value={tax}
                             onChange={(e) => setTax(e.target.value)}
                             />
                         </div>
                         <div className="form-actions">
                              <button type="submit" className="btn">Add Product</button>
                         </div>
                     </form>
                 </Modal>

    </div>
  );
};

export default DashboardPage;

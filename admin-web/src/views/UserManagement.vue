<template>
  <div class="user-management">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" size="small" @click="openUserModal()">添加用户</el-button>
        </div>
      </template>
      
      <div class="toolbar">
        <el-input
          v-model="userSearchKeyword"
          placeholder="搜索用户名或邮箱"
          style="width: 200px"
          @keyup.enter="searchUsers"
        />
        <el-button type="primary" @click="searchUsers">搜索</el-button>
      </div>
      
      <el-table :data="userList" style="width: 100%" v-loading="userLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="roles" label="角色" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role.id" style="margin-right: 4px">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editUser(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="openRoleModal(row)">分配角色</el-button>
            <el-button size="small" type="danger" @click="deleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="userPage"
          v-model:page-size="userPageSize"
          :page-sizes="[10, 20, 50]"
          :total="userTotal"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchUserList"
          @current-change="fetchUserList"
        />
      </div>
    </el-card>
  </div>

  <div v-if="currentMenu === 'roles'" class="role-management">
    <el-card class="role-card">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" size="small" @click="openRoleModal()">添加角色</el-button>
        </div>
      </template>
      
      <el-table :data="roleList" style="width: 100%" v-loading="roleLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="120" />
        <el-table-column prop="code" label="角色编码" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editRole(row)">编辑</el-button>
            <el-button size="small" type="success" @click="openPermissionModal(row)">分配权限</el-button>
            <el-button size="small" type="danger" @click="deleteRole(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="rolePage"
          v-model:page-size="rolePageSize"
          :page-sizes="[10, 20, 50]"
          :total="roleTotal"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchRoleList"
          @current-change="fetchRoleList"
        />
      </div>
    </el-card>
  </div>

  <div v-if="currentMenu === 'permissions'" class="permission-management">
    <el-card class="permission-card">
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <el-button type="primary" size="small" @click="openPermissionModal()">添加权限</el-button>
        </div>
      </template>
      
      <el-table :data="permissionList" style="width: 100%" v-loading="permissionLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="权限名称" width="150" />
        <el-table-column prop="code" label="权限编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editPermission(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deletePermission(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="permissionPage"
          v-model:page-size="permissionPageSize"
          :page-sizes="[10, 20, 50]"
          :total="permissionTotal"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchPermissionList"
          @current-change="fetchPermissionList"
        />
      </div>
    </el-card>
  </div>

  <el-dialog v-model="showUserModal" :title="userForm.id ? '编辑用户' : '添加用户'" width="500px">
    <el-form :model="userForm" label-width="80px">
      <el-form-item label="用户名">
        <el-input v-model="userForm.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="userForm.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="性别">
        <el-select v-model="userForm.gender" placeholder="请选择性别">
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
          <el-option label="保密" value="保密" />
        </el-select>
      </el-form-item>
      <el-form-item label="生日">
        <el-date-picker v-model="userForm.birthday" type="date" placeholder="选择生日" />
      </el-form-item>
      <el-form-item label="个性签名">
        <el-input v-model="userForm.signature" type="textarea" :rows="2" placeholder="请输入个性签名" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showUserModal = false">取消</el-button>
      <el-button type="primary" @click="saveUser">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showRoleModal" :title="roleForm.id ? '编辑角色' : '添加角色'" width="500px">
    <el-form :model="roleForm" label-width="80px">
      <el-form-item label="角色名称">
        <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
      </el-form-item>
      <el-form-item label="角色编码">
        <el-input v-model="roleForm.code" placeholder="请输入角色编码（如： SUPER_ADMIN） />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="roleForm.description" type="textarea" :rows="2" placeholder="请输入角色描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showRoleModal = false">取消</el-button>
      <el-button type="primary" @click="saveRole">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showPermissionModal" :title="permissionForm.id ? '编辑权限' : '添加权限'" width="500px">
    <el-form :model="permissionForm" label-width="80px">
      <el-form-item label="权限名称">
        <el-input v-model="permissionForm.name" placeholder="请输入权限名称" />
      </el-form-item>
      <el-form-item label="权限编码">
        <el-input v-model="permissionForm.code" placeholder="请输入权限编码（如: system:manage） />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="permissionForm.description" type="textarea" :rows="2" placeholder="请输入权限描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showPermissionModal = false">取消</el-button>
      <el-button type="primary" @click="savePermission">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showRoleAssignModal" title="分配角色" width="500px">
    <el-form label-width="80px">
      <el-form-item label="当前用户">
        <el-input :value="currentUser.username" disabled />
      </el-form-item>
      <el-form-item label="选择角色">
        <el-checkbox-group v-model="selectedRoleIds">
          <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.name" :value="role.id" />
        </el-checkbox-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showRoleAssignModal = false">取消</el-button>
      <el-button type="primary" @click="saveUserRoles">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showPermissionAssignModal" title="分配权限" width="500px">
    <el-form label-width="80px">
      <el-form-item label="当前角色">
        <el-input :value="currentRole.name" disabled />
      </el-form-item>
      <el-form-item label="选择权限">
        <el-checkbox-group v-model="selectedPermissionIds">
          <el-checkbox v-for="perm in allPermissions" :key="perm.id" :label="perm.name" :value="perm.id" />
        </el-checkbox-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showPermissionAssignModal = false">取消</el-button>
      <el-button type="primary" @click="saveRolePermissions">保存</el-button>
    </template>
  </el-dialog>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../utils/api'

export default {
  name: 'UserManagement',
  setup() {
    const currentMenu = inject('currentMenu')
    
    const userSearchKeyword = ref('')
    const userList = ref([])
    const userLoading = ref(false)
    const userPage = ref(1)
    const userPageSize = ref(10)
    const userTotal = ref(0)
    const showUserModal = ref(false)
    const userForm = ref({
      id: null,
      username: '',
      email: '',
      gender: '',
      birthday: '',
      signature: ''
    })

    
    const roleList = ref([])
    const roleLoading = ref(false)
    const rolePage = ref(1)
    const rolePageSize = ref(10)
    const roleTotal = ref(0)
    const showRoleModal = ref(false)
    const roleForm = ref({
      id: null,
      name: '',
      code: '',
      description: ''
    })
    const allRoles = ref([])
    const showRoleAssignModal = ref(false)
    const currentUser = ref({})
    const selectedRoleIds = ref([])
    
    const permissionList = ref([])
    const permissionLoading = ref(false)
    const permissionPage = ref(1)
    const permissionPageSize = ref(10)
    const permissionTotal = ref(0)
    const showPermissionModal = ref(false)
    const permissionForm = ref({
      id: null,
      name: '',
      code: '',
      description: ''
    })
    const allPermissions = ref([])
    const showPermissionAssignModal = ref(false)
    const currentRole = ref({})
    const selectedPermissionIds = ref([])

    const formatDate = (dateStr) => {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN')
    }

    const fetchUserList = async () => {
      userLoading.value = true
      try {
        const response = await api.get('/admin/users', {
          params: {
            page: userPage.value,
            pageSize: userPageSize.value,
            keyword: userSearchKeyword.value
          }
        })
        if (response.data.success) {
          userList.value = response.data.data
          userTotal.value = response.data.total
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        ElMessage.error('获取用户列表失败')
      } finally {
        userLoading.value = false
      }
    }
    
    const searchUsers = () => {
      userPage.value = 1
      fetchUserList()
    }
    
    const fetchRoleList = async () => {
      roleLoading.value = true
      try {
        const response = await api.get('/role/list')
        if (response.data.success) {
          roleList.value = response.data.data
          roleTotal.value = response.data.data ? response.data.data.length : 0
        }
      } catch (error) {
        console.error('获取角色列表失败:', error)
        ElMessage.error('获取角色列表失败')
      } finally {
        roleLoading.value = false
      }
    }
    
    const fetchPermissionList = async () => {
      permissionLoading.value = true
      try {
        const response = await api.get('/role/permissions/all')
        if (response.data.success) {
          permissionList.value = response.data.data
          permissionTotal.value = response.data.data ? response.data.data.length : 0
        }
      } catch (error) {
        console.error('获取权限列表失败:', error)
        ElMessage.error('获取权限列表失败')
      } finally {
        permissionLoading.value = false
      }
    }
    
    const openUserModal = (user = null) => {
      if (user) {
        userForm.value = {
          id: user.id,
          username: user.username,
          email: user.email || '',
          gender: user.gender || '',
          birthday: user.birthday || '',
          signature: user.signature || ''
        }
      } else {
        userForm.value = {
          id: null,
          username: '',
          email: '',
          gender: '',
          birthday: '',
          signature: ''
        }
      }
      showUserModal.value = true
    }
    
    const editUser = (user) => {
      openUserModal(user)
    }
    
    const saveUser = async () => {
      try {
        let response
        if (userForm.value.id) {
          response = await api.put(`/admin/user/${userForm.value.id}`, userForm.value)
        } else {
          response = await api.post('/admin/user/add', userForm.value)
        }
        
        if (response.data.success) {
          ElMessage.success(response.data.message)
          showUserModal.value = false
          fetchUserList()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        console.error('保存用户失败:', error)
        ElMessage.error('保存用户失败')
      }
    }
    
    const deleteUser = async (user) => {
      if (!confirm(`确定要删除用户 "${user.username}" 吗？`)) return
      
      try {
        const response = await api.delete(`/admin/user/${user.id}`)
        if (response.data.success) {
          ElMessage.success('删除成功')
          fetchUserList()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        ElMessage.error('删除用户失败')
      }
    }
    
    const openRoleModal = (role = null) => {
      if (role) {
        roleForm.value = {
          id: role.id,
          name: role.name,
          code: role.code,
          description: role.description || ''
        }
      } else {
        roleForm.value = {
          id: null,
          name: '',
          code: '',
          description: ''
        }
      }
      showRoleModal.value = true
    }
    
    const editRole = (role) => {
      openRoleModal(role)
    }
    
    const saveRole = async () => {
      try {
        let response
        if (roleForm.value.id) {
          response = await api.put('/role/update', roleForm.value)
        } else {
          response = await api.post('/role/add', roleForm.value)
        }
        
        if (response.data.success) {
          ElMessage.success(response.data.message)
          showRoleModal.value = false
          fetchRoleList()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        console.error('保存角色失败:', error)
        ElMessage.error('保存角色失败')
      }
    }
    
    const deleteRole = async (role) => {
      if (!confirm(`确定要删除角色 "${role.name}" 吗？`)) return
      
      try {
        const response = await api.delete(`/role/delete/${role.id}`)
        if (response.data.success) {
          ElMessage.success('删除成功')
          fetchRoleList()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        console.error('删除角色失败:', error)
        ElMessage.error('删除角色失败')
      }
    }
    
    const openPermissionModal = (permission = null) => {
      if (permission) {
        permissionForm.value = {
          id: permission.id,
          name: permission.name,
          code: permission.code,
          description: permission.description || ''
        }
      } else {
        permissionForm.value = {
          id: null,
          name: '',
          code: '',
          description: ''
        }
      }
      showPermissionModal.value = true
    }
    
    const editPermission = (permission) => {
      openPermissionModal(permission)
    }
    
    const savePermission = async () => {
      try {
        let response
        if (permissionForm.value.id) {
          response = await api.put('/role/permissions/update', permissionForm.value)
        } else {
          response = await api.post('/role/permissions/add', permissionForm.value)
        }
        
        if (response.data.success) {
          ElMessage.success(response.data.message)
          showPermissionModal.value = false
          fetchPermissionList()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        console.error('保存权限失败:', error)
        ElMessage.error('保存权限失败')
      }
    }
    
    const deletePermission = async (permission) => {
      if (!confirm(`确定要删除权限 "${permission.name}" 吗？`)) return
      
      try {
        const response = await api.delete(`/role/permissions/delete/${permission.id}`)
        if (response.data.success) {
          ElMessage.success('删除成功')
          fetchPermissionList()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        console.error('删除权限失败:', error)
        ElMessage.error('删除权限失败')
      }
    }
    
    const openRoleModal = async (user) => {
      currentUser.value = user
      try {
        const response = await api.get('/role/list')
        if (response.data.success) {
          allRoles.value = response.data.data
        }
      } catch (error) {
        console.error('获取角色列表失败:', error)
      }
      
      try {
        const response = await api.get(`/role/user/${user.id}/roles`)
        if (response.data.success) {
          selectedRoleIds.value = response.data.data.map(r => r.id)
        }
      } catch (error) {
        console.error('获取用户角色失败:', error)
      }
      
      showRoleAssignModal.value = true
    }
    
    const saveUserRoles = async () => {
      try {
        const response = await api.post(`/admin/user/${currentUser.value.id}/roles`, {
          roleIds: selectedRoleIds.value
        })
        if (response.data.success) {
          ElMessage.success('角色分配成功')
          showRoleAssignModal.value = false
          fetchUserList()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        console.error('角色分配失败:', error)
        ElMessage.error('角色分配失败')
      }
    }
    
    const openPermissionModal = async (role) => {
      currentRole.value = role
      try {
        const response = await api.get('/role/permissions/all')
        if (response.data.success) {
          allPermissions.value = response.data.data
        }
      } catch (error) {
        console.error('获取权限列表失败:', error)
      }
      
      try {
        const response = await api.get(`/role/permissions/${role.id}`)
        if (response.data.success) {
          selectedPermissionIds.value = response.data.data.map(p => p.id)
        }
      } catch (error) {
        console.error('获取角色权限失败:', error)
      }
      
      showPermissionAssignModal.value = true
    }
    
    const saveRolePermissions = async () => {
      try {
        const response = await api.post('/role/permissions/assign`, {
          roleId: currentRole.value.id,
          permissionIds: selectedPermissionIds.value
        })
        if (response.data.success) {
          ElMessage.success('权限分配成功')
          showPermissionAssignModal.value = false
          fetchRoleList()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        console.error('权限分配失败:', error)
        ElMessage.error('权限分配失败')
      }
    }
    
    watch(currentMenu, (newMenu) => {
      if (newMenu === 'users') {
        fetchUserList()
      }
      if (newMenu === 'roles') {
        fetchRoleList()
      }
      if (newMenu === 'permissions') {
        fetchPermissionList()
      }
    })
    
    onMounted(() => {
      fetchUserList()
      fetchRoleList()
      fetchPermissionList()
    })
    
    return {
      currentMenu,
      userSearchKeyword,
      userList,
      userLoading,
      userPage,
      userPageSize,
      userTotal,
      showUserModal,
      userForm,
      editUser,
      saveUser,
      deleteUser,
      searchUsers,
      roleList,
      roleLoading,
      rolePage,
      rolePageSize,
      roleTotal,
      showRoleModal,
      roleForm,
      editRole,
      saveRole,
      deleteRole,
      allRoles,
      showRoleAssignModal,
      currentUser,
      selectedRoleIds,
      openRoleModal,
      saveUserRoles,
      permissionList,
      permissionLoading,
      permissionPage,
      permissionPageSize,
      permissionTotal,
      showPermissionModal,
      permissionForm,
      editPermission,
      savePermission,
      deletePermission,
      allPermissions,
      showPermissionAssignModal,
      currentRole,
      selectedPermissionIds,
      openPermissionModal,
      saveRolePermissions,
      formatDate,
      fetchUserList,
      fetchRoleList,
      fetchPermissionList
    }
  }
</script>

<style scoped>
.user-management, .role-management, .permission-management {
    padding: 24px;
  }

  .user-card, .role-card, .permission-card {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  min-height: 500px;
  height: fit-content;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
  }

  .toolbar {
    margin-bottom: 16px;
    display: flex;
    gap: 12px;
  }
</style>
